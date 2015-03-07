$(document).ready(function() {

    var snakeWidth = 10;
    var canvasElem = $('canvas');
    var id = $(canvasElem).attr('id');
    var canvas = canvasElem[0];
    var context = canvas.getContext("2d");
    wsEndpoint = location.host+':9000';
    //TODO this is ugly
    if(location.host.indexOf(':') > -1){
        wsEndpoint = location.host; //do not append the port if already connecting on a non default port
    }
    var soc = new WebSocket("ws://"+wsEndpoint+"/gamesocket/"+id);
    soc.onmessage = function(event){
        points = JSON.parse(event.data);
        clearPoints(points[1],context);
        addPoints(points[0],context);
    };

    var infoElem = $("#gameinfo");

    var infoSoc = new WebSocket("ws://"+wsEndpoint+"/gameinfosocket/"+id);
    infoSoc.onmessage = function(event){
        info = JSON.parse(event.data)
        infoElem.empty();
        for(i = 0 ; i < info.length ; i++){
            infoElem.append($("<div>"+info[i].name+" "+info[i].length+"</div>"))
        }

    }

    addPoints = function(points,context){
        for(i=0;i<points.length;i++){
            xpos = points[i].x * snakeWidth;
            ypos = points[i].y * snakeWidth;
            color = points[i].c;
            context.fillStyle = color;
            context.fillRect(xpos,ypos ,snakeWidth,snakeWidth);
        }
    };
    clearPoints = function(points,context){
        for(i=0;i<points.length;i++){
            xpos = points[i].x * snakeWidth;
            ypos = points[i].y * snakeWidth;
            context.clearRect(xpos,ypos ,snakeWidth,snakeWidth);
        }
    };

    $(document).on("keypress",function(event){
        if(event.keyCode == 106){
            soc.send("l");
        }
        else if(event.keyCode == 108){
            soc.send("r");
        }
    });

    $(document).on("scrollstart",false);

    $(document).on("swipeleft",function(event){
        event.stopPropagation();
        event.preventDefault();
        soc.send("l");
    });
    $(document).on("swiperight",function(event){
        event.stopPropagation();
        event.preventDefault();
        soc.send("r");
    });
});

