package com.exe201.project.enums;

public enum FeatureType {
    CREATE_MULTIPLE_WALLETS("Create Multiple Wallets", "Ability to create multiple wallets"),
    UNLIMITED_TRANSACTIONS("Unlimited Transactions", "No limit on number of transactions"),
    ADVANCED_ANALYTICS("Advanced Analytics", "Access to advanced financial analytics"),
    EXPORT_DATA("Export Data", "Ability to export financial data"),
    PRIORITY_SUPPORT("Priority Support", "Access to priority customer support"),
    CUSTOM_CATEGORIES("Custom Categories", "Create custom transaction categories"),
    BUDGET_ALERTS("Budget Alerts", "Set up budget alerts and notifications"),
    FINANCIAL_GOALS("Financial Goals", "Set and track financial goals"),
    MULTI_CURRENCY("Multi Currency", "Support for multiple currencies");

    private final String displayName;
    private final String description;

    FeatureType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
