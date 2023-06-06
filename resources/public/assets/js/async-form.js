const submitForm = (form) => {
    const formData = new FormData(form);
    const errorNode = document.createTextNode("Something went wrong while submitting your form, please try again.");
    form.classList.add("loading");

    fetch(form.action, {
        method: 'POST',
        body: new URLSearchParams(formData),
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    }).then(response => {
        if (response.ok) {
          console.log('Form submitted successfully!');
          form.classList.remove("loading");
          form.parentElement.classList.add("success");
          
        } else {
          // Error
          console.error('Form submission error:', response.status);
          form.classList.remove("loading");
          form.parentElement.classList.add("error");
          form.children[1].children[0].appendChild(errorNode);
        }
    })
    .catch(function(error) {
        console.error('Form submission error:', error);
        form.classList.remove("loading");
        form.parentElement.classList.add("error");
        form.children[1].children[0].appendChild(errorNode);
        // Display error message or perform other error handling
    });

    return false;
}
