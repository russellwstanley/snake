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
            gameId = gamesMsg[i].id;
            gameName = gamesMsg[i].name;
            if(!(gameId in games)){
                gamesElem.append("<a href=games/"+gameId+" style=\"display:block\">"+gameName+"</a>");
//                gameSocket = new WebSocket("ws://"+wsEndpoint+"/gamesocket/"+gameId);
//                games[gameId] = gameSocket


            }
        }
    };
});
