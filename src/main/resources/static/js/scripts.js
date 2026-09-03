// =========================================================
// MediCore HMS — client-side interactions
// =========================================================
document.addEventListener('DOMContentLoaded', function () {
    const toggleBtn = document.querySelector('.sidebar-toggle');
    const sidebar = document.querySelector('.sidebar');

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', function () {
            sidebar.classList.toggle('open');
        });

        document.addEventListener('click', function (event) {
            if (window.innerWidth > 992) return;
            const clickedInsideSidebar = sidebar.contains(event.target);
            const clickedToggle = toggleBtn.contains(event.target);
            if (!clickedInsideSidebar && !clickedToggle) {
                sidebar.classList.remove('open');
            }
        });
    }

    // Auto-dismiss alerts after a few seconds
    document.querySelectorAll('.alert').forEach(function (alertEl) {
        setTimeout(function () {
            alertEl.style.transition = 'opacity 0.4s ease';
            alertEl.style.opacity = '0';
            setTimeout(function () { alertEl.remove(); }, 400);
        }, 5000);
    });
});
