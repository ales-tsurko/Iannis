IannisNodeGroup : Group {
  var <getState, 
  // used as a container for the voices
  // instantiated by the midi client
  <>midiVoices;

  set {arg ...args;
    super.set(*args);

    getState??{getState = ()};

    args.do({arg item, n;
      if (n.even) {
        getState[item] = args[n+1]
      }
    });
  }

  releaseMIDIVoices {
    this.midiVoices.do(_.release);
  }
}
