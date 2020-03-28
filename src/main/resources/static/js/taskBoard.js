function openViewItemModal(projectId, itemId, sprintId) {
    $('.modal-body').load('/user/project/' + projectId + '/item/' + itemId + '/?source=taskBoardPage&sprintIdModal=' + sprintId, function () {
        $('#viewItemModal').modal({show: true});
    });
}

function redirectToModal(projectId, itemId, sprintId) {
    $('.modal-body').load('/user/project/' + projectId + '/item/' + itemId + '/?source=taskBoardPage&sprintIdModal=' + sprintId, function () {
        $('#viewItemModal').modal({show: true});
    });
}

function updateAssignee(itemId) {
    $('#updateAssignee' + itemId).click();
}