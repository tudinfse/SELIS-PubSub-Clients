var delim = new Buffer('\n');
var bsplit = require('buffer-split')
var Client = require('node-rest-client').Client;
var bodyParser = require('body-parser');
var base64= require('base-64');
var utf8 = require('utf8');
var net = require('net');
var express = require('express');
var app = express();



if (process.argv.length >= 2) {
       if (process.argv[2] == 'dev') {
               app.set('hostname', (process.env.HOST || '192.168.2.16'));
       } else if (process.argv[2] == 'prod') {
               app.set('hostname', (process.env.HOST || '147.102.4.183'));
       } else {
               console.log('Usage node index.js {dev|prod}');
               process.exit(1);
       }
} else {
       console.log('Usage node index.js {dev|prod}');
       process.exit(1);
}

app.set('port', (process.env.PORT || 5000))
var jsonParser = bodyParser.json();
var socket = net.Socket();
app.use(jsonParser);
socket.connect(20001, app.get('hostname'), function() {
    console.log('connecting...');
});
socket.on('error', function(err) {
    console.log(err);
    console.log(arguments);
});

socket.on('connect', function() {
    console.log('Connected');
    data='{"authHash" : "GPSSimulator"}\n';
    var sent=socket.write(data,function(){
      console.log('authHash sent');
    });
    console.log(sent);

});
socket.on('data', function(data) {

  console.log('got data');
  if(!data.includes("\n")){ //depending on the dataset size the data might be broken on multiple messages
    totaldata=totaldata+data;
  }
  else{
    var arr=bsplit(data, delim);
    totaldata=totaldata+arr[0];
    var json=JSON.parse(totaldata.toString());
    var base64st=json.find(function(json){
    return json.key=="payload";
  });
  }
  var bytes=base64.decode(base64st.val);
  var struct = utf8.decode(bytes);
  //the struct variable contains the decoded final message in JSON Format
  parseStruct(stuct);
  totaldata=arr[1];
});

function parseStruct(json){
  var struct = JSON.parse(json);
  //logic for parsing incoming messages should go here

  //example showing how to post back on the PUB/SUB following below. This can also be in a seperate function
  var parsedStruct;
  var jsonstring = JSON.stringify(parsedStruct);
  var utfstr = utf8.encode(jsonstring);
  var encstr = base64.encode(utfstr);
  //this encodes the value in base64 format


  //below you can see how the publication struct for posting on the pub/sub is formed
  var publication= {
     "publication": [
      {
         "key": "topic",
         "type": "string",
         "val": "your_topic"
      },
       {
           "key": "your_topic",
           "type": "string",
           "val": "some_value" //acts as a topic identifier
       },
       {
           "key": "payload",
           "type": "string",
           "val": encstr  //encoded payload value
       }
   ]
};
  var args = {
    data :publication,
    headers: { "Content-Type": "application/json" }

  };

    var client= new Client();
    client.post(app.get('hostname') + ":" + app.get('port') + "/publish", args, function (data, response) {});
}

socket.on('end', function() {
  console.log("GOODBYE :( ");
});