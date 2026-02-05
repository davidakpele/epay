function processCard(action) {
    const loader = document.getElementById('global-loader');
    const loaderMsg = loader.querySelector('p');
    
    loaderMsg.innerText = action === 'Approve' ? 'Communicating with Mastercard API...' : 'Declining Request...';
    loader.style.display = 'flex';
    
    setTimeout(() => {
        loader.style.display = 'none';
        alert('Request ' + (action === 'Approve' ? 'Approved' : 'Declined') + ' Successfully.');
    }, 2000);
}