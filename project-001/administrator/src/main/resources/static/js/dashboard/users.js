$(function () {
    $('#example1').DataTable()
    $('#example2').DataTable({
      'paging'      : true,
      'lengthChange': false,
      'searching'   : true,
      'ordering'    : true,
      'info'        : true,
      'autoWidth'   : false
    })
  })

const API_BASE_URL = "http://localhost:8292/api";

async function apiRequest(url, options = {}) {
    const token = localStorage.getItem('access_token');

    try {
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                ...(token && { 'Authorization': `Bearer ${token}` }),
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            const err = await response.json().catch(() => ({}));
            throw new Error(err.message || `HTTP Error: ${response.status}`);
        }

        if (response.status === 204) return { success: true };
        return await response.json();
    } catch (error) {
        throw error;
    }
}

async function fetchUsers(page = 0, size = 100) {
    return await apiRequest(`${API_BASE_URL}/user/list?page=${page}&size=${size}`);
}

async function fetchUserById(userId) {
    return await apiRequest(`${API_BASE_URL}/user/${userId}`);
}

async function requestDeleteUser(userId) {
    return await apiRequest(`${API_BASE_URL}/user/${userId}`, { method: 'DELETE' });
}

async function requestBlockUser(userId, block) {
    return await apiRequest(`${API_BASE_URL}/user/${userId}/block`, {
        method: 'POST',
        body: JSON.stringify({ block: block })
    });
}

async function requestLockUser(userId, lock) {
    return await apiRequest(`${API_BASE_URL}/user/${userId}/lock`, {
        method: 'POST',
        body: JSON.stringify({ lock: lock })
    });
}

let currentUserId   = null;
let currentUserData = null;

async function loadUsers() {
    try {
        showProcessDialog();
        const response = await fetchUsers();
        const users = response.content || [];
        await new Promise(resolve => setTimeout(resolve, 3000));
        renderTable(users);
        closeProcessDialog();
    } catch (error) {
        closeProcessDialog();
        showResult('Error', error.message, 'error');
    }
}

function renderTable(users) {
    const tbody = document.querySelector('#example1 tbody');
    if (!tbody) return;

    if ($.fn.DataTable.isDataTable('#example1')) {
        $('#example1').DataTable().destroy();
    }

    tbody.innerHTML = '';

    if (users.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">No users found.</td></tr>`;
        return;
    }

    users.forEach((user, index) => {
        const profile  = Array.isArray(user.records) && user.records.length > 0 ? user.records[0] : {};
        const userId   = user.id;
        const fullName = [profile.firstName, profile.lastName].filter(Boolean).join(' ') || 'N/A';
        const gender   = profile.gender ? capitalise(profile.gender) : 'N/A';
        const photo    = profile.photo  || '/dist/img/user3-128x128.jpg';
        const status   = resolveStatus(user.enabled, profile.locked, profile.isBlocked);
        const kyc      = profile.isTransferPinSet ? '100%' : '0%';
        const joined   = formatDate(user.createdOn || user.updatedOn);

        const blockLabel = profile.isBlocked
            ? '<i class="fa fa-check-circle" aria-hidden="true"></i> Unblock Account'
            : '<i class="fa fa-ban" aria-hidden="true"></i> Block Account';

        const lockLabel = profile.locked
            ? '<i class="fa fa-unlock" aria-hidden="true"></i> Unlock Account'
            : '<i class="fa fa-lock" aria-hidden="true"></i> Lock Account';

        tbody.insertAdjacentHTML('beforeend', `
            <tr>
                <td>${index + 1}.</td>
                <td>
                    <div class="user-profile">
                        <img src="${escapeHtml(photo)}"
                             class="img-circle avatar"
                             alt="${escapeHtml(fullName)}"
                             onerror="this.src='/dist/img/user3-128x128.jpg'">
                    </div>
                </td>
                <td>${escapeHtml(fullName)}</td>
                <td>${escapeHtml(gender)}</td>
                <td><span class="label ${getStatusClass(status)}">${status}</span></td>
                <td>${escapeHtml(kyc)}</td>
                <td>${escapeHtml(joined)}</td>
                <td>
                    <div class="action-dropdown">
                        <button class="action-btn btn btn-block btn-primary" onclick="toggleDropdown(this)">
                            Actions <i class="fa fa-chevron-down" aria-hidden="true"></i>
                        </button>
                        <ul class="dropdown-menu">
                            <li>
                                <a href="#"
                                   data-user-id="${userId}"
                                   data-is-blocked="${!!profile.isBlocked}"
                                   onclick="openBlockDialog(event, this)">
                                    ${blockLabel}
                                </a>
                            </li>
                            <li>
                                <a href="#"
                                   data-user-id="${userId}"
                                   data-is-locked="${!!profile.locked}"
                                   onclick="openLockDialog(event, this)">
                                    ${lockLabel}
                                </a>
                            </li>
                            <li>
                                <a href="/admin/user/${userId}/view">
                                    <i class="fa fa-eye" aria-hidden="true"></i> View Profile
                                </a>
                            </li>
                            <li>
                                <a href="/admin/user/${userId}/edit">
                                    <i class="fa fa-pencil" aria-hidden="true"></i> Edit Profile
                                </a>
                            </li>
                            <li class="danger">
                                <a href="#"
                                   data-user-id="${userId}"
                                   onclick="openDeleteDialog(event, this)">
                                    <i class="fa fa-trash" aria-hidden="true"></i> Delete Account
                                </a>
                            </li>
                        </ul>
                    </div>
                </td>
            </tr>
        `);
    });

    if (typeof $.fn.DataTable !== 'undefined') {
        $('#example1').DataTable({
            paging:       true,
            lengthChange: true,
            searching:    true,
            ordering:     true,
            info:         true,
            autoWidth:    false
        });
    }
}

function resolveStatus(enabled, locked, isBlocked) {
    if (isBlocked) return 'Blocked';
    if (locked)    return 'Locked';
    if (enabled)   return 'Active';
    return 'Inactive';
}

function getStatusClass(status) {
    const map = {
        'Active':   'label-success',
        'Blocked':  'label-danger',
        'Locked':   'label-warning',
        'Inactive': 'label-default'
    };
    return map[status] || 'label-default';
}

function capitalise(str) {
    if (!str) return '';
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date   = new Date(dateString);
    const day    = date.getDate();
    const month  = date.toLocaleString('en-US', { month: 'short' });
    const year   = date.getFullYear();
    const s      = [,'st','nd','rd'];
    const suffix = s[(day % 100 >> 3) ^ 1 && day % 10] || 'th';
    return `${day}${suffix} ${month} ${year}`;
}

function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(text);
    return div.innerHTML;
}

function openDeleteDialog(event, anchor) {
    event.preventDefault();
    currentUserId = anchor.dataset.userId;
    closeAllDropdowns();
    document.getElementById('deleteDialog').style.display = 'flex';
}

function closeDeleteDialog() {
    document.getElementById('deleteDialog').style.display = 'none';
    currentUserId = null;
}

function openBlockDialog(event, anchor) {
    event.preventDefault();
    currentUserId   = anchor.dataset.userId;
    const isBlocked = anchor.dataset.isBlocked === 'true';
    currentUserData = { isBlocked };
    closeAllDropdowns();

    const title   = document.querySelector('#blockDialog .confirm-title');
    const message = document.querySelector('#blockDialog .confirm-message');
    const btn     = document.querySelector('#blockDialog .confirm-btn.danger');

    if (isBlocked) {
        title.textContent   = 'Unblock Account';
        message.textContent = 'This will restore the user\'s access to the system. Do you want to continue?';
        btn.textContent     = 'Yes, Unblock';
    } else {
        title.textContent   = 'Block Account';
        message.textContent = 'Blocking this account will prevent the user from accessing the system. Do you want to continue?';
        btn.textContent     = 'Yes, Block';
    }

    document.getElementById('blockDialog').style.display = 'flex';
}

function closeBlockDialog() {
    document.getElementById('blockDialog').style.display = 'none';
    currentUserId   = null;
    currentUserData = null;
}

function openLockDialog(event, anchor) {
    event.preventDefault();
    currentUserId   = anchor.dataset.userId;
    const isLocked  = anchor.dataset.isLocked === 'true';
    currentUserData = { isLocked };
    closeAllDropdowns();

    const title   = document.querySelector('#lockDialog .confirm-title');
    const message = document.querySelector('#lockDialog .confirm-message');
    const btn     = document.querySelector('#lockDialog .confirm-btn.danger');

    if (isLocked) {
        title.textContent   = 'Unlock Account';
        message.textContent = 'This will restore temporary access for the user. Do you want to continue?';
        btn.textContent     = 'Yes, Unlock';
    } else {
        title.textContent   = 'Lock Account';
        message.textContent = 'Locking this account will temporarily restrict access. You can unlock it later.';
        btn.textContent     = 'Yes, Lock';
    }

    document.getElementById('lockDialog').style.display = 'flex';
}

function closeLockDialog() {
    document.getElementById('lockDialog').style.display = 'none';
    currentUserId   = null;
    currentUserData = null;
}

function showProcessDialog() {
    document.getElementById('processDialog').style.display = 'flex';
}

function closeProcessDialog() {
    document.getElementById('processDialog').style.display = 'none';
}

function showResult(title, message, type = 'success') {
    document.getElementById('resultTitle').textContent    = title;
    document.getElementById('resultMessage').textContent  = message;
    document.getElementById('resultTitle').className      = `confirm-title ${type}`;
    document.getElementById('resultDialog').style.display = 'flex';
}

function closeResultDialog() {
    document.getElementById('resultDialog').style.display = 'none';
}

async function processDelete() {
    if (!currentUserId) return;
    const userId = currentUserId;
    closeDeleteDialog();
    showProcessDialog();
    try {
        await requestDeleteUser(userId);
        closeProcessDialog();
        showResult('Success', 'User account has been deleted successfully.', 'success');
        await loadUsers();
    } catch (error) {
        closeProcessDialog();
        showResult('Error', error.message, 'error');
    }
}

async function processBlock() {
    if (!currentUserId) return;
    const userId    = currentUserId;
    const isBlocked = currentUserData?.isBlocked;
    const payload   = isBlocked ? false : true;
    closeBlockDialog();
    showProcessDialog();
    try {
        await requestBlockUser(userId, payload);
        closeProcessDialog();
        showResult('Success', payload ? 'User account has been blocked successfully.' : 'User account has been unblocked successfully.', 'success');
        await loadUsers();
    } catch (error) {
        closeProcessDialog();
        showResult('Error', error.message, 'error');
    }
}

async function processLock() {
    if (!currentUserId) return;
    const userId   = currentUserId;
    const isLocked = currentUserData?.isLocked;
    const payload  = isLocked ? false : true;
    closeLockDialog();
    showProcessDialog();
    try {
        await requestLockUser(userId, payload);
        closeProcessDialog();
        showResult('Success', payload ? 'User account has been locked successfully.' : 'User account has been unlocked successfully.', 'success');
        await loadUsers();
    } catch (error) {
        closeProcessDialog();
        showResult('Error', error.message, 'error');
    }
}

function toggleDropdown(button) {
    const dropdown = button.nextElementSibling;
    const isOpen   = dropdown.classList.contains('show');
    closeAllDropdowns();
    if (!isOpen) dropdown.classList.add('show');
}

function closeAllDropdowns() {
    document.querySelectorAll('.dropdown-menu.show').forEach(m => m.classList.remove('show'));
}

document.addEventListener('click', function (e) {
    if (!e.target.closest('.action-dropdown')) closeAllDropdowns();
});

document.addEventListener('DOMContentLoaded', function () {
    loadUsers();
});