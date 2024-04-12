/** Apply read more click logic to page */
let elements = document.querySelectorAll('.wrap .right');
if (elements.length > 1) {
    elements[0].classList.add('full');
    elements[1].classList.add('full');
}
elements.forEach(function(element) {
    let paragraph = element.querySelector('.job-description p');
    let range = document.createRange();
    range.selectNodeContents(paragraph);
    let rect = range.getBoundingClientRect();
    let twoRowsHeight = parseFloat(window.getComputedStyle(paragraph).lineHeight) * 2;

    if (rect.height <= twoRowsHeight) {
        element.classList.add('full');
    } else {
        element.addEventListener('click', function() {
            this.classList.toggle('full');
        });
    }
});
