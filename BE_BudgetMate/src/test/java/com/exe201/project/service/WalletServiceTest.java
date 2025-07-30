package com.exe201.project.service;

import com.exe201.project.dto.request.WalletRequest;
import com.exe201.project.entity.Users;
import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.WalletType;
import com.exe201.project.exception.OutOfPermissionException;
import com.exe201.project.mapper.WalletMapper;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.repository.WalletRepository;
import com.exe201.project.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private MembershipAccessService membershipAccessService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Users testUser;
    private WalletRequest defaultWalletRequest;
    private WalletRequest savingsWalletRequest;
    private WalletRequest debtWalletRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new Users();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Setup wallet requests
        defaultWalletRequest = new WalletRequest(
            "My Default Wallet",
            WalletType.DEFAULT,
            1000.0,
            null,  // interestRate
            null,  // deadline
            null,  // startDate
            null   // termMonths
        );

        savingsWalletRequest = new WalletRequest(
            "My Savings Wallet",
            WalletType.SAVINGS,
            5000.0,
            5.0,   // interestRate
            null,  // deadline will be calculated
            LocalDate.now(),  // startDate
            12     // termMonths
        );

        debtWalletRequest = new WalletRequest(
            "My Debt Wallet",
            WalletType.DEBT,
            3000.0,
            null,  // interestRate
            LocalDate.now().plusMonths(6),  // deadline
            null,  // startDate
            null   // termMonths
        );

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    void createWallet_ShouldAllowFirstDefaultWallet() {
        // Given
        when(walletRepository.findAllByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(walletRepository.save(any(Wallets.class))).thenReturn(new Wallets());

        // When & Then - Should not throw exception
        walletService.createWallet(defaultWalletRequest);

        verify(walletRepository, times(1)).save(any(Wallets.class));
    }

    @Test
    void createWallet_ShouldRejectSecondDefaultWallet() {
        // Given - User already has one DEFAULT wallet
        Wallets existingDefaultWallet = new Wallets();
        existingDefaultWallet.setType(WalletType.DEFAULT);
        List<Wallets> existingWallets = List.of(existingDefaultWallet);
        
        when(walletRepository.findAllByUserId(testUser.getId())).thenReturn(existingWallets);

        // When & Then
        assertThrows(OutOfPermissionException.class, () -> {
            walletService.createWallet(defaultWalletRequest);
        });

        verify(walletRepository, never()).save(any(Wallets.class));
    }

    @Test
    void createWallet_ShouldRejectSavingsWallet_WhenNoPermission() {
        // Given
        when(walletRepository.findAllByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(membershipAccessService.canCreateSavingsWallets(testUser.getId())).thenReturn(false);

        // When & Then
        assertThrows(OutOfPermissionException.class, () -> {
            walletService.createWallet(savingsWalletRequest);
        });

        verify(walletRepository, never()).save(any(Wallets.class));
    }

    @Test
    void createWallet_ShouldAllowSavingsWallet_WhenHasPermissionAndWithinLimit() {
        // Given
        when(walletRepository.findAllByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(membershipAccessService.canCreateSavingsWallets(testUser.getId())).thenReturn(true);
        when(membershipAccessService.getFeatureLimit(testUser.getId(), "CREATE_SAVINGS_WALLET")).thenReturn(3);
        when(walletRepository.save(any(Wallets.class))).thenReturn(new Wallets());

        // When & Then - Should not throw exception
        walletService.createWallet(savingsWalletRequest);

        verify(walletRepository, times(1)).save(any(Wallets.class));
    }

    @Test
    void createWallet_ShouldRejectSavingsWallet_WhenAtLimit() {
        // Given - User already has maximum allowed SAVINGS wallets
        Wallets existingSavings1 = new Wallets();
        existingSavings1.setType(WalletType.SAVINGS);
        Wallets existingSavings2 = new Wallets();
        existingSavings2.setType(WalletType.SAVINGS);
        List<Wallets> existingWallets = List.of(existingSavings1, existingSavings2);
        
        when(walletRepository.findAllByUserId(testUser.getId())).thenReturn(existingWallets);
        when(membershipAccessService.canCreateSavingsWallets(testUser.getId())).thenReturn(true);
        when(membershipAccessService.getFeatureLimit(testUser.getId(), "CREATE_SAVINGS_WALLET")).thenReturn(2);

        // When & Then
        assertThrows(OutOfPermissionException.class, () -> {
            walletService.createWallet(savingsWalletRequest);
        });

        verify(walletRepository, never()).save(any(Wallets.class));
    }

    @Test
    void createWallet_ShouldRejectDebtWallet_WhenNoPermission() {
        // Given
        when(walletRepository.findAllByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(membershipAccessService.canCreateDeptWallets(testUser.getId())).thenReturn(false);

        // When & Then
        assertThrows(OutOfPermissionException.class, () -> {
            walletService.createWallet(debtWalletRequest);
        });

        verify(walletRepository, never()).save(any(Wallets.class));
    }

    @Test
    void createWallet_ShouldAllowDebtWallet_WhenHasPermissionAndWithinLimit() {
        // Given
        when(walletRepository.findAllByUserId(testUser.getId())).thenReturn(new ArrayList<>());
        when(membershipAccessService.canCreateDeptWallets(testUser.getId())).thenReturn(true);
        when(membershipAccessService.getFeatureLimit(testUser.getId(), "CREATE_DEPT_WALLET")).thenReturn(2);
        when(walletRepository.save(any(Wallets.class))).thenReturn(new Wallets());

        // When & Then - Should not throw exception
        walletService.createWallet(debtWalletRequest);

        verify(walletRepository, times(1)).save(any(Wallets.class));
    }

    @Test
    void createWallet_ShouldRejectDebtWallet_WhenAtLimit() {
        // Given - User already has maximum allowed DEBT wallets
        Wallets existingDebt1 = new Wallets();
        existingDebt1.setType(WalletType.DEBT);
        List<Wallets> existingWallets = List.of(existingDebt1);
        
        when(walletRepository.findAllByUserId(testUser.getId())).thenReturn(existingWallets);
        when(membershipAccessService.canCreateDeptWallets(testUser.getId())).thenReturn(true);
        when(membershipAccessService.getFeatureLimit(testUser.getId(), "CREATE_DEPT_WALLET")).thenReturn(1);

        // When & Then
        assertThrows(OutOfPermissionException.class, () -> {
            walletService.createWallet(debtWalletRequest);
        });

        verify(walletRepository, never()).save(any(Wallets.class));
    }
}
