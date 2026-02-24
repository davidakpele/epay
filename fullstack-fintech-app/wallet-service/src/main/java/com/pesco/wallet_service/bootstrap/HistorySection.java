package com.pesco.wallet_service.bootstrap;
import lombok.Data;
import java.util.List;

@Data
public class HistorySection {
    private List<TransactionRecord> deposit_container;
    private List<TransactionRecord> withdraws_container;
    private List<TransactionRecord> swap_container;
    private List<TransactionRecord> services_container;
    private List<TransactionRecord> transfer_container;
    private List<TransactionRecord> maintenances_container;

    public HistorySection() {
    }

    public HistorySection(List<TransactionRecord> deposit_container, List<TransactionRecord> withdraws_container, List<TransactionRecord> swap_container, List<TransactionRecord> services_container, List<TransactionRecord> transfer_container, List<TransactionRecord> maintenances_container) {
        this.deposit_container = deposit_container;
        this.withdraws_container = withdraws_container;
        this.swap_container = swap_container;
        this.services_container = services_container;
        this.transfer_container = transfer_container;
        this.maintenances_container = maintenances_container;
    }


    public List<TransactionRecord> getDeposit_container() {
        return this.deposit_container;
    }

    public void setDeposit_container(List<TransactionRecord> deposit_container) {
        this.deposit_container = deposit_container;
    }

    public List<TransactionRecord> getWithdraws_container() {
        return this.withdraws_container;
    }

    public void setWithdraws_container(List<TransactionRecord> withdraws_container) {
        this.withdraws_container = withdraws_container;
    }

    public List<TransactionRecord> getSwap_container() {
        return this.swap_container;
    }

    public void setSwap_container(List<TransactionRecord> swap_container) {
        this.swap_container = swap_container;
    }

    public List<TransactionRecord> getServices_container() {
        return this.services_container;
    }

    public void setServices_container(List<TransactionRecord> services_container) {
        this.services_container = services_container;
    }

    public List<TransactionRecord> getTransfer_container() {
        return this.transfer_container;
    }

    public void setTransfer_container(List<TransactionRecord> transfer_container) {
        this.transfer_container = transfer_container;
    }

    public List<TransactionRecord> getMaintenances_container() {
        return this.maintenances_container;
    }

    public void setMaintenances_container(List<TransactionRecord> maintenances_container) {
        this.maintenances_container = maintenances_container;
    }

}

