var errMsg = "may not be null";
var divErr = "<div class='error'>" + errMsg + "</div>";
const notifierType = Object.freeze({RABBITMQ:"RabbitMQ", SNS:"SNS"});

function showRabbitmq(show) {
    $(".rabbitmq-config").each(function() {
        show ? $(this).show() : $(this).hide();
    });
}

function showSNS(show) {
    $(".sns-config").each(function() {
        show ? $(this).show() : $(this).hide();
    });
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

    if ($("#notifiertype").val() === notifierType.SNS) {
        showRabbitmq(false);
    } else {
        showSNS(false);
    }

    $("#notifiertype").change(function () {
        if ($(this).val() === notifierType.SNS) {
            showRabbitmq(false);
            showSNS(true);
        } else {
            showSNS(false);
            showRabbitmq(true);
        }
    });

    $("form#globalProperties").submit(function(e) {
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