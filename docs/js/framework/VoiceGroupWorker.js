/**
* ElkPublic is a JavaScript library that makes it easier
* to manage the HTML of your application and
*  interact with the Java servlet ElkServer.
* More details at <https://github.com/tncrazvan/ElkServer/>.
* Copyright (C) 2016-2018  Tanase Razvan Catalin
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

let ws = null;
const CONNECTED = 0, DISCONNECTED = 1;
self.onmessage=function(e){
  if(e.data.connect){
    ws = new WebSocket(e.data.connect);
    ws.onopen=function(e){
      postMessage({status: CONNECTED});
      console.log("Connected to VoiceGroup");
    };
    ws.onmessage=function(e){
      //console.log("Received from server",e.data);
      postMessage(e.data);
    };
    ws.onclose=function(e){
      postMessage({status: DISCONNECTED});
      console.log("Disconnected from VoiceGroup");
    };

  }else if(e.data.disconnect){
    ws.close();
  }else {
    ws.send(e.data);
  }
};
