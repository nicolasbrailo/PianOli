package com.nicobrailo.pianoli;

class SpyCallback implements AppConfigTrigger.AppConfigCallback {
    int triggerCount = 0;
    int toastCount = 0;

    @Override
    public void requestConfig() {
        triggerCount++;
    }

    @Override
    public void showConfigTooltip() {
        toastCount++;
    }
}
