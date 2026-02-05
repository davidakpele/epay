function processLoan(action, user) {
    const loader = document.getElementById('global-loader');
    const loaderMsg = loader.querySelector('p');
    
    loaderMsg.innerText = action === 'Approve' ? `Disbursing funds to ${user}...` : `Sending rejection notice to ${user}...`;
    loader.style.display = 'flex';
    
    setTimeout(() => {
        loader.style.display = 'none';
        alert(`Loan ${action === 'Approve' ? 'Approved & Disbursed' : 'Rejected'} Successfully.`);
    }, 2000);
}