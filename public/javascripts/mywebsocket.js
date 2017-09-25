$(document).ready(function() {
    var endpoint = document.getElementById("websocket").getAttribute("data-endpoint");

    var webSocket = $.simpleWebSocket({url: " ws://" + window.location.host + endpoint});

    // reconnected listening
    webSocket.listen(function (message) {
        console.log(" Received " + message);

        var prettyJson = JSON.stringify(JSON.parse(message), null, 2);

        // Update textarea
        $("#response").val(prettyJson + "\r\n" + $("#response").val())
    });

    function sendMessage(msg) {
        webSocket.send(msg).done(function () {
            console.log(" Message \"" + msg + "\" sent successfully");
        }).fail(function (e) {
            console.log(" Could not send message, " + e);
        });
    }

    $("#message-form").submit(function (event) {
        // Send the message and clear the input field
        sendMessage($("#message").val());
        $("#message").val("");
    })
})