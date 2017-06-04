IannisNodeGroup : Group {
  var <getState, 
  <>allowedNumberOfVoices,
  // used as a container for the voices
  // instantiated by the midi client
  <midiVoices;

  *new {arg target, addAction = 'addToHead';
    ^super.new(target, addAction).init();
  }

  init {
    allowedNumberOfVoices = 4;
    midiVoices = [];
    getState = ();
  }

  set {arg ...args;
    super.set(*args);

    args.do({arg item, n;
      if (n.even) {
        getState[item] = args[n+1]
      }
    });
  }

  releaseMIDIVoices {
    this.midiVoices.do(_.release);
  }

  addVoice {arg keyNum, synth;
    midiVoices = midiVoices.addAll([keyNum, synth]);

    if (midiVoices.size > (allowedNumberOfVoices*2)) {
      // remove keynum
      midiVoices.removeAt(0);
      // free the voice and remove it
      midiVoices[0].free();
      midiVoices.removeAt(0);
    }
  }

  releaseVoice {arg keyNum;
    var numIndex = midiVoices.indexOf(keyNum);
    numIndex!?{
      // remove keyNum
      midiVoices.removeAt(numIndex);

      // release associated voice
      midiVoices[numIndex].release();
      midiVoices.removeAt(numIndex);
    }
  }

  getVoice {arg keyNum;
    var numIndex = midiVoices.indexOf(keyNum);
    ^midiVoices[numIndex+1];
  }
}
