function isChrome() {
  var isChromium = window.chrome,
    winNav = window.navigator,
    vendorName = winNav.vendor,
    isOpera = winNav.userAgent.indexOf("OPR") > -1,
    isIEedge = winNav.userAgent.indexOf("Edge") > -1,
    isIOSChrome = winNav.userAgent.match("CriOS");

  if(isIOSChrome){
    return true;
  } else if(isChromium !== null && isChromium !== undefined && vendorName === "Google Inc." && isOpera == false && isIEedge == false) {
    return true;
  } else {
    return false;
  }
}

function gotoListeningState() {
  const micListening = document.querySelector(".mic .listening");
  const micReady = document.querySelector(".mic .ready");

  micListening.style.display = "block";
  micReady.style.display = "none";
}

function gotoReadyState() {
  const micListening = document.querySelector(".mic .listening");
  const micReady = document.querySelector(".mic .ready");

  micListening.style.display = "none";
  micReady.style.display = "block";
}

function addBotItem(text) {
  const appContent = document.querySelector(".app-content");
  appContent.innerHTML += '<div class="item-container item-container-bot"><div class="item"><p>' + text + '</p></div></div>';
  appContent.scrollTop = appContent.scrollHeight; // scroll to bottom
}

function addUserItem(text) {
  const appContent = document.querySelector(".app-content");
  appContent.innerHTML += '<div class="item-container item-container-user"><div class="item"><p>' + text + '</p></div></div>';
  appContent.scrollTop = appContent.scrollHeight; // scroll to bottom
}

function displayCurrentTime() {
  const timeContent = document.querySelector(".time-indicator-content");
  const d = new Date();
  const s = d.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
  timeContent.innerHTML = s;
}

function addError(text) {
  addBotItem(text);
  const footer = document.querySelector(".app-footer");
  footer.style.display = "none";
}

document.addEventListener("DOMContentLoaded", function(event) {

  // test for relevant API-s
  // for (let api of ['speechSynthesis', 'webkitSpeechSynthesis', 'speechRecognition', 'webkitSpeechRecognition']) {
  //   console.log('api ' + api + " and if browser has it: " + (api in window));
  // }

  // check for Chrome
  if (!isChrome()) {
    addError("This demo only works in Google Chrome.");
    return;
  }

  if (!('speechSynthesis' in window)) {
    addError("Your browser doesn’t support speech synthesis. This demo won’t work.");
    return;
  }

  if (!('webkitSpeechRecognition' in window)) {
    addError("Your browser cannot record voice. This demo won’t work.");
    return;
  }

  var initialContent = true;
  function handleResponse(serverResponse) {

      // Set a timer just in case. so if there was an error speaking or whatever, there will at least be a prompt to continue
      var timer = window.setTimeout(function() { startListening(); }, 5000);
      var jsonServerResponse = JSON.parse(serverResponse);
      
      const speech = (jsonServerResponse["result"]["fulfillment"]["speech"] === undefined) ? jsonServerResponse["result"]["fulfillment"]["messages"][0]["speech"][0] : jsonServerResponse["result"]["fulfillment"]["speech"];
      const displayText = (jsonServerResponse["result"]["fulfillment"]["displayText"] === undefined) ? jsonServerResponse["result"]["fulfillment"]["messages"][0]["speech"][0] : jsonServerResponse["result"]["fulfillment"]["displayText"];
      
      var msg = new SpeechSynthesisUtterance(speech);
      var voices = window.speechSynthesis.getVoices();
      msg.default = false;
      msg.voice = voices.filter(function(voice) { return voice.name == 'Google UK English Female'; })[0];
      msg.lang = 'en-GB';

      addBotItem(displayText);
      if(initialContent!== undefined && !initialContent){
    	  runAliceCommand(displayText);
      }
      /*ga('send', 'event', 'Message', 'add', 'bot');*/
      msg.addEventListener("end", function(ev) {
        window.clearTimeout(timer);
        startListening();
      });
      msg.addEventListener("error", function(ev) {
        window.clearTimeout(timer);
        startListening();
      });

      
      window.speechSynthesis.speak(msg);
    }
    function handleError(serverError) {
      console.log("Error from api.ai server: ", serverError);
    }
    
  function ajax(options) {
	return new Promise(function(resolve, reject) {
	   $.ajax(options).done(resolve).fail(reject);
	});
  }
  
  const timeIndicatorContent = document.querySelector(".time-indicator-content").innerHTML;
  if(timeIndicatorContent === undefined || timeIndicatorContent === ""){
	  displayCurrentTime();
	  //Now we’ve established that the browser is Chrome with proper speech API-s.
	
	  // api.ai client
	  //const apiClient = new ApiAi.ApiAiClient({accessToken: '13f191c473134f38a31d4232ca319f9b'});
	
	  // Initial feedback message.
	  //addBotItem("Hi! I’m Alice. Logging you in ...");
	  // addBotItem("Hi! I’m Alice. Tap the microphone and start talking to me.");
	  
	  let promise = ajax({ url: "/CaseWorkerPortal/ai" , data: {"query": "Log in with 101 / abcd"}});
	
	  initialContent = true;
	  promise
	      .then(handleResponse)
	      .catch(handleError);
	
	  var recognition = new webkitSpeechRecognition();
	  var recognizedText = null;
	  recognition.continuous = false;
	  recognition.interimResults = true;
	  recognition.onstart = function() {
	    recognizedText = null;
	  };
	  recognition.onresult = function(ev) {
	    //recognizedText = ev["results"][0][0]["transcript"];
	
	    var interim_transcript = '';
	    recognizedText = '';

	    for (var i = ev.resultIndex; i < ev.results.length; ++i) {
	      if (ev.results[i].isFinal) {
	    	recognizedText += ev.results[i][0].transcript;
	    	addUserItem(recognizedText);
	      } else {
	        interim_transcript += ev.results[i][0].transcript;
	      }
	    }
	    
	    //final_transcript = capitalize(final_transcript);
	    //final_span.innerHTML = linebreak(final_transcript);
	    //addUserItem(recognizedText);
	    $('#transcript').val(interim_transcript);
	    
	    
	    /*ga('send', 'event', 'Message', 'add', 'user');*/
	
	    //let promise = apiClient.textRequest(recognizedText);
	    let promise = ajax({ url: "/CaseWorkerPortal/ai" , data: {"query": recognizedText}});
	
	    initialContent = true;
	    promise
	        .then(handleResponse)
	        .catch(handleError);
	
	    
	  };
	
	  recognition.onerror = function(ev) {
	    console.log("Speech recognition error", ev);
	  };
	  recognition.onend = function() {
	    gotoReadyState();
	  };
  } else{
	  var result = runAliceFilterCommand($('.item-container:last .item').text());
	  console.log('result is: '+result);
	  addBotItem('Your filtered result is ready.');
  }
  
  function startListening() {
    gotoListeningState();
    recognition.start();
  }

  const startButton = document.querySelector("#start");
  startButton.addEventListener("click", function(ev) {
    //ga('send', 'event', 'Button', 'click');
    startListening();
    ev.preventDefault();
  });
  
  $("#transcriptButton").on("click", function(){
	 var dummyText = $('#transcript').val();
	 addUserItem(dummyText);
	 let promise = ajax({ url: "/CaseWorkerPortal/ai" , data: {"query": dummyText}});
	 $('#transcript').val('');
	 
	 initialContent = false;
	    promise
	        .then(handleResponse)
	        .catch(handleError);
  });
  

  // Esc key handler - cancel listening if pressed
  // http://stackoverflow.com/questions/3369593/how-to-detect-escape-key-press-with-javascript-or-jquery
  document.addEventListener("keydown", function(evt) {
    evt = evt || window.event;
    var isEscape = false;
    if ("key" in evt) {
        isEscape = (evt.key == "Escape" || evt.key == "Esc");
    } else {
        isEscape = (evt.keyCode == 27);
    }
    if (isEscape) {
        recognition.abort();
    }
  });


});
