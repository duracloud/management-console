var errMsg = "may not be null";
var divErr = "<div class='error'>" + errMsg + "</div>";
const queueType = Object.freeze({RABBITMQ:"RabbitMQ", SQS:"SQS"});

function showRabbitmq(show) {
    $(".rabbitmq-config").each(function() {
        show ? $(this).show() : $(this).hide();
    });
}

function showRabbitmqHost(show) {
    $(".rabbitmq-host-config").each(function() {
        show ? $(this).show() : $(this).hide();
    });
}

function hideGlobalPropsCheckbox() {
    if($("#globalPropsRmqConfAvailable").val() === "false") {
        $("#globalPropsRmqConfCheckbox").hide();
    }
}

$(document).ready(function () {
    $("input").on("input", function(e) {
        if ($(this).val() === "" || $(this).val() === null) {
            $(this).addClass("error");
            $(divErr).insertAfter(this);
        } else {
            $(this).removeClass("error");
            $(this).siblings(".error").remove();
            $(this).next().remove();
        }
    });

    if($("#queuetype").val() === queueType.SQS){
        showRabbitmq(false);
    } else {
        if($("#globalPropsRmqConf").is(":checked")){
            showRabbitmqHost(false);
        }
    }

    hideGlobalPropsCheckbox();

    $("#queuetype").change(function () {
        if ($(this).val() === queueType.SQS) {
            showRabbitmq(false);
        } else {
            showRabbitmq(true);
            hideGlobalPropsCheckbox();
        }
    });

    $("#globalPropsRmqConf").change(function () {
        $(".rabbitmq-host-config").toggle(!this.checked);
    });

    $("form#duracloudMill").submit(function(e) {
        var allgood = true;
        $("input:visible").each(function() {
            if ($(this).val() === "" || $(this).val() === null) {
                $(this).addClass("error");
                if ($(this).next().text() !== errMsg) {
                    $(divErr).insertAfter(this);
                }
                e.preventDefault();
                allgood = false;
            } else {
                $(this).removeClass("error");
                $(this).siblings(".error").remove();
            }
        });
        return allgood;
    });
});