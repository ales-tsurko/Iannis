IannisNodeGroup : Group {
  var <getState,
  voices,
  <>allowedNumberOfVoices = 4,
  <>monophonicMode = \legato; // \normal, \legato

  *new {arg target, addAction = 'addToHead';
    ^super.new(target, addAction).init();
  }

  init {
    getState = ();
    voices = [];
    // this.initResponseOSCFunction();
  }

  // initResponseOSCFunction {
    // var func = OSCFunc({arg msg;
      // var newNodeID = msg[1];
      // var groupID = msg[2];
      // if (groupID == this.nodeID) {
        // ("added a synth with ID:"+newNodeID).postln;
      // }
    // }, '/n_go', this.server.addr);
// 
    // this.onFree({func.free()});
  // }
// 
  set {arg ...args;
    super.set(*args);

    args.do({arg item, n;
      if (n.even) {
        getState[item] = args[n+1]
      }
    });
  }
}
