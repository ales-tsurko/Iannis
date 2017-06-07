IannisVoicesManager {
  var <>allowedNumberOfVoices,
  <>monophonicMode, // \normal or \legato
  voices;

  *new {arg numberOfVoices = 4;
    ^super.new.init(numberOfVoices);
  }

  init {arg voicesNum;
    allowedNumberOfVoices = voicesNum;
    monophonicMode = \legato;
    voices = [];
  }

  initVoice {arg keyNum, synthDefName, values, group;
    case
    // monophonic
    // legato
    {allowedNumberOfVoices == 1 && monophonicMode == \legato} {
      if (voices.size == 0) {
        this.pushVoiceIntoArray(keyNum, synthDefName, values, group);
      } {
        // change keynum
        voices[0] = keyNum;
        // update the voice's values
        voices[1].set(*values);
      };
    }
    // polyphonic and retrigger monophonic (\normal monophonicMode)
    {true} {
      this.pushVoiceIntoArray(keyNum, synthDefName, values, group);

      if (voices.size > (allowedNumberOfVoices*2)) {
        // remove keynum
        voices.removeAt(0);
        // free the voice and remove it
        voices[0].free();
        voices.removeAt(0);
      }
    };
  }

  pushVoiceIntoArray {arg keyNum, synthDefName, values, group;
    var newVoice = Synth(
      synthDefName,
      values, 
      group
    );
    voices = voices.addAll([keyNum, newVoice]);
  }

  releaseVoice {arg keyNum;
    case
    // monophonic \legato
    {allowedNumberOfVoices == 1 && monophonicMode == \legato} {
      // release the voice if its current keynum
      // is equal to previous keynum
      if (voices[0] == keyNum) {
        voices[1].release();
        voices = [];
      }
    }
    // polyphonic and retrigger monophonic (\normal monophonicMode)
    {true} {
      var numIndex = voices.indexOf(keyNum);
      numIndex!?{
        // remove keyNum
        voices.removeAt(numIndex);

        // release associated voice
        voices[numIndex].release();
        voices.removeAt(numIndex);
      }
    };
  }

  getVoice {arg keyNum;
    var numIndex = voices.indexOf(keyNum);
    ^voices[numIndex+1];
  }

  releaseAll {
    voices.do({arg item, n;
      if (n.odd) {
        item.release();
      }
    });
  }
}
