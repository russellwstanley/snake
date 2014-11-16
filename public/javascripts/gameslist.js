$(document).ready(function() {
    var gamesElem = $('#games');
    var games = {}
    var snakeWidth = 3;
    var canvasSize = 61*snakeWidth

    var processPoints = function(points,action){
        for(i=0;i<points.length;i++){
            xpos = points[i][0] * snakeWidth;
            ypos = points[i][1] * snakeWidth;
            action(xpos,ypos ,snakeWidth,snakeWidth);
        }
    };

    wsEndpoint = location.host+':9000';
    //TODO this is ugly
    if(location.host.indexOf(':') > -1){
        wsEndpoint = location.host; //do not append the port if already connecting on a non default port
    }
    soc = new WebSocket("ws://"+wsEndpoint+"/gamessocket");
    soc.onmessage = function(event){
        JSON.parse(event.data).forEach(function(game){
            if(!(game.id in games)){
                var canvasElem = $("<canvas id="+game.id+" width="+canvasSize+" height="+canvasSize+" class=\"clear\" style=\"border:1px dotted;float:center\"></canvas>")
                var titleElem = $("<a href=games/"+game.id+" width="+canvasSize+">"+game.name+"</a>")

                gamesElem.append(titleElem);
                gamesElem.append(canvasElem);
                var context = canvasElem[0].getContext('2d');
                var gameSocket = new WebSocket("ws://"+wsEndpoint+"/watchgamesocket/"+game.id);
                games[game.id] = gameSocket;
                //TODO massive duplication with snake.js
                gameSocket.onmessage = function(event){
                    points = JSON.parse(event.data);
                    processPoints(points[0],context.fillRect.bind(context)); //create new points
                    processPoints(points[1],context.clearRect.bind(context)); //delete old points
                }
            }
        });
    };
});
