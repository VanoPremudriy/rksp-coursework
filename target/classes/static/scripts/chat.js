let _data;
let _user;
function connect() {
    var socket = new SockJS('/chat-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log("connected: " + frame);
        stompClient.subscribe('/chat_topic/auth_user', function(response) {
            _user = response.body;
            stompClient.send("/chat_controller/get_messages");
        });
        stompClient.subscribe('/chat_topic/messages', function(response) {
            _data = JSON.parse(response.body);
            draw(_data, _user);
        });
        stompClient.subscribe('/chat_topic/one_message', function(response) {
            _data = JSON.parse(response.body);
            addOneMess(_data, _user);
        });
        stompClient.send("/chat_controller/get_user");
    });
}


function draw(data, user) {
    let $sub = $('.card-body');
    for (let key in data) {
        let $model;
        if (data[key].sender.username.localeCompare(user)){
             $model = '<span>\n' +
                '                        <div class="d-flex flex-row justify-content-start">\n' +
                '                            <img class="avatar" src=" ' + data[key].sender.avatar + ' "\n' +
                '                                 alt="avatar 1">\n' +
                '                            <div style="width:50%;">\n' +
                '                                <p class="small p-3 ms-3 mb-1 rounded-3 my-mess">' + data[key].mess  + '</p>\n' +
                '                            </div>\n' +
                '                        </div>\n' +
                '                        </span>';
        }
        else{
             $model = '<span>\n' +
                '                              <div class="d-flex flex-row justify-content-end mb-4 pt-1">\n' +
                '                                <div style="width:50%; margin-right: 10px">\n' +
                '                                    <p class="small p-3 me-3 mb-1 text-white rounded-3 friend-mess">'+ data[key].mess +'</p>\n' +
                '                                </div>\n' +
                '                                <img class="avatar" src="'+ data[key].sender.avatar +'" \n' +
                '                                     alt="avatar 1">\n' +
                '                        </div>\n' +
                '                        </span>';
        }
        $sub.append($model)
    }
}


function addMess(){
    stompClient.send("/chat_controller/send", {}, $("#exampleFormControlInput1").val());
}

function addOneMess(data, user) {
    let $sub = $('.card-body');
    let $model;
    if (data.sender.username.localeCompare(user)){
        $model = '<span>\n' +
            '                        <div class="d-flex flex-row justify-content-start">\n' +
            '                            <img class="avatar" src=" ' + data.sender.avatar + ' "\n' +
            '                                 alt="avatar 1">\n' +
            '                            <div style="width:50%;">\n' +
            '                                <p class="small p-3 ms-3 mb-1 rounded-3 my-mess">' + data.mess  + '</p>\n' +
            '                            </div>\n' +
            '                        </div>\n' +
            '                        </span>';
    }
    else{
        $model = '<span>\n' +
            '                              <div class="d-flex flex-row justify-content-end mb-4 pt-1">\n' +
            '                                <div style="width:50%; margin-right: 10px">\n' +
            '                                    <p class="small p-3 me-3 mb-1 text-white rounded-3 friend-mess">'+ data.mess +'</p>\n' +
            '                                </div>\n' +
            '                                <img class="avatar" src="'+ data.sender.avatar +'" \n' +
            '                                     alt="avatar 1">\n' +
            '                        </div>\n' +
            '                        </span>';
    }
    $sub.append($model)
    $("#exampleFormControlInput1").val('');
    $sub.animate({
        scrollTop: 10000000},100);
}