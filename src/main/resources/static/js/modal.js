document.addEventListener('DOMContentLoaded', () => {
    const modalEl = document.getElementById('app-modal');
    if (!modalEl) return;
    const modal = new bootstrap.Modal(modalEl);

    document.body.addEventListener('click', async (e) => {
        const trigger = e.target.closest('[data-toggle="ajax-modal"]');
        if (trigger) {
            e.preventDefault();
            const url = trigger.getAttribute('href');
            
            try {
                const response = await fetch(url, {
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                });
                const html = await response.text();
                modalEl.querySelector('.modal-content').innerHTML = html;
                modal.show();
            } catch (err) {
                console.error(err);
                alert('Failed to load form.');
            }
        }
    });

    // Handle form submission inside modal
    modalEl.addEventListener('submit', async (e) => {
        if (e.target.tagName === 'FORM') {
            e.preventDefault();
            const form = e.target;
            const formData = new FormData(form);
            // Handle URL-encoded form data if strictly required by Spring, but FormData usually works with standard binding
            // Spring MVC @ModelAttribute handles multipart/form-data (which FormData sends) fine usually, 
            // but let's send it as URL-encoded just in case or rely on FormData.
            // Actually, for @ModelAttribute, FormData (multipart) is fine.
            
            const url = form.action;
            
            try {
                const response = await fetch(url, {
                    method: 'POST',
                    body: formData,
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                });
                
                if (response.ok) {
                    const text = await response.text();
                    if (text === 'SUCCESS') {
                        modal.hide();
                        window.location.reload(); 
                    } else {
                        // Validation error, replace content
                        modalEl.querySelector('.modal-content').innerHTML = text;
                    }
                } else {
                     alert('An error occurred.');
                }
            } catch (err) {
                console.error(err);
                alert('Failed to save.');
            }
        }
    });
});
