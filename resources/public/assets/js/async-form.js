const submitForm = (form) => {
    const formData = new FormData(form);
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
          var errorText = document.createTextNode("Something went wrong while submitting your form, please try again.");
          form.children[0].children[0].appendChild(errorText);
        }
    })
    .catch(function(error) {
        console.error('Form submission error:', error);
        form.classList.remove("loading");
        form.parentElement.classList.add("error");
        var errorText = document.createTextNode("Something went wrong while submitting your form, please try again.");
        form.children[0].children[0].appendChild(errorText);
        // Display error message or perform other error handling
    });

    return false;
}
