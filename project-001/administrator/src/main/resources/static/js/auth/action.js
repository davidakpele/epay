const togglePassword = document.getElementById('togglePassword');
const passwordField = document.getElementById('passwordField');
const loginForm = document.getElementById('loginForm');
const submitBtn = document.getElementById('submitBtn');

// Keycode input
const keycodeCells = document.querySelectorAll('.keycode-cell');
const verificationInput = document.getElementById('verificationInput');
let keycode = '';


togglePassword.addEventListener('click', () => {
    const type = passwordField.type === 'password' ? 'text' : 'password';
    passwordField.type = type;
    togglePassword.textContent = type === 'password' ? 'SHOW' : 'HIDE';
});

keycodeCells.forEach(cell => {
    cell.addEventListener('click', () => {
        const index = parseInt(cell.dataset.index);
        const num = Math.floor(Math.random() * 10); 
        
        cell.textContent = num;
        cell.classList.add('filled');
        
        keycode += num;
        verificationInput.value = keycode;
        
        // Reset after 5 digits
        if (keycode.length >= 5) {
            setTimeout(() => {
                keycodeCells.forEach(c => {
                    c.textContent = '';
                    c.classList.remove('filled');
                });
                keycode = '';
                verificationInput.value = '';
            }, 1000);
        }
    });
});

async function loginWithApi(username, password) {
    try {
        const response = await fetch('/admin/api/auth/login', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        
        const data = await response.json();
        if(!data.status || data.status !=200){
            document.querySelector(".incoming-error").style.display = 'block'
            document.querySelector(".error-message").innerHTML =data.message;
            submitBtn.classList.remove('loading');
        }else{
            submitBtn.classList.remove('loading');
            handleLoginSuccess(data);
        }
         
    } catch (err) {
        document.querySelector(".incoming-error").style.display = 'block'
        document.querySelector(".error-message").innerHTML =err;
        submitBtn.classList.remove('loading');
    } 
}


function handleLoginSuccess(data) {
    localStorage.setItem('access_token', data.accessToken);
    localStorage.setItem('user', JSON.stringify(data.user));
    localStorage.setItem('token_expires', data.expiresIn);
    window.location.href = '/admin/dashboard';
}

loginForm.addEventListener('submit', (e) => {
    e.preventDefault();
    document.querySelector(".incoming-error").style.display = 'none'
    document.querySelector(".error-message").innerHTML ="";
    // Show loading
    submitBtn.classList.add('loading');

    const consoleOutput = document.querySelector('.console-output');
    const newLine = document.createElement('div');
    newLine.className = 'console-line';
    newLine.textContent = '[AUTH] Authentication sequence initiated...';
    consoleOutput.appendChild(newLine);
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('passwordField').value;
    setTimeout(() => {
        loginWithApi(username, password);
    }, 1500);
});

// Simulate console updates
function updateConsole() {
    const consoleOutput = document.querySelector('.console-output');
    const messages = [
        '[SECURITY] Firewall status: ACTIVE',
        '[MONITOR] No active threats detected',
        '[SYSTEM] All services operational',
        '[AUDIT] Log rotation completed'
    ];
    
    const randomMessage = messages[Math.floor(Math.random() * messages.length)];
    const newLine = document.createElement('div');
    newLine.className = 'console-line';
    newLine.textContent = randomMessage;
    
    consoleOutput.appendChild(newLine);
    
    // Keep only last 10 lines
    const lines = consoleOutput.querySelectorAll('.console-line');
    if (lines.length > 10) {
        lines[0].remove();
    }
    
    // Scroll to bottom
    consoleOutput.scrollTop = consoleOutput.scrollHeight;
}

// Update console every 30 seconds
setInterval(updateConsole, 30000);

// Simulate status updates
function updateStatus() {
    const activeSessions = document.querySelector('.status-line:nth-child(2) .status-value');
    const sessions = Math.floor(Math.random() * 3);
    activeSessions.textContent = sessions;
    activeSessions.style.color = sessions === 0 ? '#666' : sessions === 1 ? '#ffbd2e' : '#00ff88';
}

// Update status every 20 seconds
setInterval(updateStatus, 20000);

// Simulate typing effect on page load
document.addEventListener('DOMContentLoaded', () => {
    const cursor = document.querySelector('.console-cursor');
    let visible = true;
    
    setInterval(() => {
        cursor.style.opacity = visible ? '0' : '1';
        visible = !visible;
    }, 500);
});

// Input focus effects
document.querySelectorAll('.input-field').forEach(input => {
    input.addEventListener('focus', () => {
        input.parentElement.style.borderBottomColor = '#00ff88';
    });
    
    input.addEventListener('blur', () => {
        input.parentElement.style.borderBottomColor = '#333';
    });
});

function isTokenExpired() {
    const expires = localStorage.getItem('token_expires');
    if (!expires) return true;
    
    const expiryDate = new Date(expires);
    const now = new Date();
    return now > expiryDate;
}


function setupAuthInterceptor() {
    const originalFetch = window.fetch;
    
    window.fetch = async function(url, options = {}) {
        // Add Authorization header for API requests
        if (url.startsWith('/admin/') && !url.includes('/admin/api/auth/login')) {
            const token = localStorage.getItem('access_token');
            if (token) {
                options.headers = {
                    ...options.headers,
                    'Authorization': `Bearer ${token}`
                };
            }
        }
        
        const response = await originalFetch(url, options);
        
        // Handle 401 Unauthorized
        if (response.status === 401) {
            // Clear token and redirect to login
            localStorage.removeItem('access_token');
            localStorage.removeItem('user');
            localStorage.removeItem('token_expires');
            window.location.href = '/admin/login';
            return response;
        }
        
        return response;
    };
}


document.addEventListener('DOMContentLoaded', () => {
    setupAuthInterceptor();
    
    // Check if already has valid token
    const token = localStorage.getItem('access_token');
    if (token && !isTokenExpired() && window.location.pathname === '/admin/login') {
        window.location.href = '/admin/dashboard';
    }

    // Clear token if expired
    if (isTokenExpired()) {
        localStorage.removeItem('access_token');
        localStorage.removeItem('user');
        localStorage.removeItem('token_expires');
    }
});

