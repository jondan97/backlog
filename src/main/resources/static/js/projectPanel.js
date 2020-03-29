function showCreateProjectTable() {
    document.getElementById("createProjectTable").hidden = false;
}

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


