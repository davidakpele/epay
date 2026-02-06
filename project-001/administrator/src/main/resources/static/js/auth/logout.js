// Function to clear login data
function clearLoginData() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('user');
    localStorage.removeItem('token_expires');
    localStorage.removeItem('refresh_token'); 
}

// Call this function when user clicks logout
document.addEventListener('DOMContentLoaded', function() {
    clearLoginData();
    setTimeout(function() {
        window.location.href = '/admin/login?logout=true';
    }, 2000);
});