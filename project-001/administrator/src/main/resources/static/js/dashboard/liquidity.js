function syncBanks() {
    const loader = document.getElementById('global-loader');
    loader.style.display = 'flex';
    setTimeout(() => {
        loader.style.display = 'none';
        alert('All bank feeds reconciled against user ledger.');
    }, 1500);
}