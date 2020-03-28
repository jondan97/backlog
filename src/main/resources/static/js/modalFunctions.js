function toggleAssigneeInput() {
    document.getElementById("uniqueItemAssigneeId").hidden = false;
    document.getElementById("uniqueItemAssigneeIdSpan").hidden = true;
    document.getElementById("uniqueItemChangeAssigneeButton").hidden = true;
}

function updateUniqueAssignee() {
    $('#uniqueUpdateAssigneeButton').click();
}

function modifyItem() {
    $('#viewUniqueItemTable .informationText').hide();
    $('#uniqueItemModifyButton').hide();
    //same elements so they need to be turned back on
    $('#uniqueItemStatusInput').show();
    $('#uniqueItemTotalEffortInput').show();
    $('#uniqueItemOwnerInput').show();
    $('#uniqueItemDateCreatedInput').show();
    document.getElementById("uniqueItemAssigneeId").removeAttribute("onchange");
    $('#viewUniqueItemTable .uniqueItemInput').attr('hidden', false);
    $('#viewUniqueItemTable .uniqueItemInput').attr('readonly', false);
    $('.uniqueItemUpdateAndDeleteButtons').attr('hidden', false);
}

function modifyComment(commentItemId) {
    $('#commentEditId' + commentItemId).attr('hidden', true);
    $('#commentUpdateId' + commentItemId).attr('hidden', false);
    $('#commentDeleteId' + commentItemId).attr('hidden', false);
    $('#commentBodyView' + commentItemId).attr('readonly', false);
}