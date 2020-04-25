function modifyItemDeveloper(type) {
    $('#viewUniqueItemTable .developerSpan').hide();
    $('#uniqueItemModifyDeveloperButton').hide();

    let uniqueItemInputClass = $('#viewUniqueItemTable .uniqueItemInputDeveloper');
    uniqueItemInputClass.attr('hidden', false);
    uniqueItemInputClass.attr('readonly', false);

    let uniqueDeveloperLabel = $('.developerLabel');
    uniqueDeveloperLabel.css("background-color", "coral");

    $('#uniqueItemUpdateButton').attr('hidden', false);

    if (type === 3 || type === 4) {
        $('#updateTitleWarning').attr('hidden', false);
        $('#uniqueItemDeleteButton').attr('hidden', false);
        $('.deletionWarning').attr('hidden', false);
    }
}

function modifyItemProductOwner() {
    $('#viewUniqueItemTable .productOwnerSpan').hide();
    $('#uniqueItemModifyProductOwnerButton').hide();

    //document.getElementById("uniqueItemAssigneeId").removeAttribute("onchange");

    let uniqueItemInputClass = $('#viewUniqueItemTable .uniqueItemInputProductOwner');
    uniqueItemInputClass.attr('hidden', false);
    uniqueItemInputClass.attr('readonly', false);
    $('#updateTitleWarning').attr('hidden', false);

    let uniqueDeveloperLabel = $('.productOwnerLabel');
    uniqueDeveloperLabel.css("background-color", "coral");

    $('#uniqueItemUpdateButton').attr('hidden', false);
    $('#uniqueItemDeleteButton').attr('hidden', false);
    $('.deletionWarning').attr('hidden', false);
}

function modifyComment(commentItemId) {
    $('#commentEditId' + commentItemId).attr('hidden', true);
    $('#commentUpdateId' + commentItemId).attr('hidden', false);
    $('#commentDeleteId' + commentItemId).attr('hidden', false);
    $('#commentBodyView' + commentItemId).attr('readonly', false);
}

function openPopup() {
    var popup = document.getElementById("commentHintPopup");
    popup.classList.toggle("show");
}

function expandList() {
    document.getElementById("uniqueItemChildren").hidden = false;
    $("#uniqueItemChildrenButtonExpand").hide();
    $("#uniqueItemChildrenButtonClose").show();
}

function closeList() {
    document.getElementById("uniqueItemChildren").hidden = true;
    $("#uniqueItemChildrenButtonClose").hide();
    $("#uniqueItemChildrenButtonExpand").show();
}

function checkItemInputs(itemId) {
    document.getElementById("itemAssigneeId" + itemId).value = document.getElementById("fakeItemAssigneeId").value;
}
