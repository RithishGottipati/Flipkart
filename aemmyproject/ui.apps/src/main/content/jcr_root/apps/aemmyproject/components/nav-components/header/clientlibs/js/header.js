document.querySelector('.menu-button').addEventListener('click', function() {
    document.querySelector('.menu-content').classList.toggle('show');
});

window.onclick = function(event) 
{
    if (!event.target.matches('.menu-button')) {
        var dropdowns = document.getElementsByClassName("menu-content");
        for (var i = 0; i < dropdowns.length; i++) {
            var openDropdown = dropdowns[i];
            if (openDropdown.classList.contains('show')) {
                openDropdown.classList.remove('show');
            }
        }
    }
}