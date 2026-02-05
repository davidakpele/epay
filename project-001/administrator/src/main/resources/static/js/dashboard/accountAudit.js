function toggleJson(btn) {
    const payload = btn.nextElementSibling;
    if (payload.style.display === 'block') {
        payload.style.display = 'none';
        btn.innerText = 'View Data';
    } else {
        payload.style.display = 'block';
        btn.innerText = 'Hide Data';
    }
}