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
  // Action dropdown
  function toggleDropdown(button) {
    const menu = button.nextElementSibling;
    menu.classList.toggle('show');
  }

  document.addEventListener('click', function (event) {
    document.querySelectorAll('.dropdown-menu').forEach(menu => {
      if (!menu.previousElementSibling.contains(event.target)) {
        menu.classList.remove('show');
      }
    });
  });

  
// delete action 
 function openDeleteDialog(event) {
    event.preventDefault();
    document.getElementById('deleteDialog').style.display = 'flex';

    document.querySelectorAll('.dropdown-menu').forEach(menu => {
      menu.classList.remove('show');
    });
  }

  function closeDeleteDialog() {
    document.getElementById('deleteDialog').style.display = 'none';
  }

  function showProcessing() {
    document.getElementById('processDialog').style.display = 'flex';
  }

  function hideProcessing() {
    document.getElementById('processDialog').style.display = 'none';
  }

  function showResult(success) {
    const title = document.getElementById('resultTitle');
    const message = document.getElementById('resultMessage');

    if (success) {
      title.textContent = 'Account Deleted';
      message.textContent = 'The account has been successfully deleted.';
    } else {
      title.textContent = 'Delete Failed';
      message.textContent =
        'Unable to delete the account. Please try again.';
    }

    document.getElementById('resultDialog').style.display = 'flex';
  }

  function closeResultDialog() {
    document.getElementById('resultDialog').style.display = 'none';
  }

  function processDelete() {
    closeDeleteDialog();
    showProcessing();

    // Simulated API call
    setTimeout(() => {
      hideProcessing();

      const isSuccess = Math.random() > 0.3; 
      showResult(isSuccess);

      if (isSuccess) {
        // Optionally remove table row or refresh
        // location.reload();
      }
    }, 2000);
  }

  function closeAllDropdowns() {
    document.querySelectorAll('.dropdown-menu').forEach(menu => {
      menu.classList.remove('show');
    });
  }

   function openBlockDialog(event) {
    event.preventDefault();
    closeAllDropdowns();
    document.getElementById('blockDialog').style.display = 'flex';
  }

  function closeBlockDialog() {
    document.getElementById('blockDialog').style.display = 'none';
  }

  function processBlock() {
    closeBlockDialog();
    showProcessing();

    // Simulated API call
    setTimeout(() => {
      hideProcessing();
      const success = Math.random() > 0.2;

      if (success) {
        showResult(
          'Account Blocked',
          'The account has been successfully blocked.'
        );
      } else {
        showResult(
          'Block Failed',
          'Unable to block the account. Please try again.'
        );
      }
    }, 1800);
  }

  /* ---------- Lock Account ---------- */

  function openLockDialog(event) {
    event.preventDefault();
    closeAllDropdowns();
    document.getElementById('lockDialog').style.display = 'flex';
  }

  function closeLockDialog() {
    document.getElementById('lockDialog').style.display = 'none';
  }

  function processLock() {
    closeLockDialog();
    showProcessing();

    // Simulated API call
    setTimeout(() => {
      hideProcessing();
      const success = Math.random() > 0.2;

      if (success) {
        showResult(
          'Account Locked',
          'The account has been locked successfully.'
        );
      } else {
        showResult(
          'Lock Failed',
          'Unable to lock the account. Please try again.'
        );
      }
    }, 1800);
  }