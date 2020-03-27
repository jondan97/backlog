function showCreateProjectTable() {
    document.getElementById("createProjectTable").hidden = false;
}

// Get the modal
var modal = document.getElementById("editProjectModal");

// Get the button that opens the modal
var btn = document.getElementById("myBtn");

// Get the <span> element that closes the modal
var span = document.getElementsByClassName("close")[0];

// When the user clicks on the button, open the modal
function modifyProject(projectCount) {
    $('#editProjectModal').modal({show: true});
    //modal.style.display = "block";
    document.getElementById("projectId").value = document.getElementById("projectId" + projectCount).value;
    document.getElementById("projectTitle").value = document.getElementById("projectTitle" + projectCount).value;
    document.getElementById("projectDescription").value = document.getElementById("projectDescription" + projectCount).value;
    document.getElementById("projectDevelopersWorking").value = document.getElementById("projectDevelopersWorking" + projectCount).value;
    document.getElementById("projectEstimatedSprintsNeeded").value = document.getElementById("projectEstimatedSprintsNeeded" + projectCount).value;
}

// When the user clicks on <span> (x), close the modal
span.onclick = function () {
    modal.style.display = "none";
}

// When the user clicks anywhere outside of the modal, close it
window.onclick = function (event) {
    if (event.target == modal) {
        modal.style.display = "none";
    }
}
