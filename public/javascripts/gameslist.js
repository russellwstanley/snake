$(document).ready(function() {
    var gamesElem = $('#games');
    var games = {}

    wsEndpoint = location.host+':9000';
    //TODO this is ugly
    if(location.host.indexOf(':') > -1){
        wsEndpoint = location.host; //do not append the port if already connecting on a non default port
    }
    soc = new WebSocket("ws://"+wsEndpoint+"/gamessocket");
    soc.onmessage = function(event){
        gamesMsg = JSON.parse(event.data);
        for (var i = 0; i < gamesMsg.length ; i++) {
            if(!(gamesMsg[i] in games)){
                gamesElem.append("<a href=games/"+gamesMsg[i].id+">"+gamesMsg[i].name+"</a>");
            }
        }
    };
});
