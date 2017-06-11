IannisVoicesManager {
  var <>allowedNumberOfVoices,
  <>monophonicMode = \legato, // \normal or \legato
  <sustainPedalIsOn = false,
  <>pitchBendSemitones = 2,
  voicesToReleaseOnSustainOff,
  voices;

  *new {arg numberOfVoices = 4;
    ^super.new.init(numberOfVoices);
  }

  init {arg voicesNum;
    allowedNumberOfVoices = voicesNum;
    voices = [];
    voicesToReleaseOnSustainOff = [];
  }

  noteOn {arg keyNum, synthDefName, values, group;
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
        var removingVoice;
        // remove keynum
        voices.removeAt(0);
        // free the voice and remove it
        voices[0].free();
        removingVoice = voices.removeAt(0);

        if (sustainPedalIsOn) {
          voicesToReleaseOnSustainOff = voicesToReleaseOnSustainOff
          .reject({arg voice; voice == removingVoice});
        }
      }
    };
  }

  pushVoiceIntoArray {arg keyNum, synthDefName, values, group;
    voices.indexOf(keyNum)??{
      var newVoice = Synth(
        synthDefName,
        values, 
        group
      );
      voices = voices.addAll([keyNum, newVoice]);
    };
  }

  noteOff {arg keyNum;
    case
    // monophonic \legato
    {allowedNumberOfVoices == 1 && monophonicMode == \legato} {
      // release the voice if its current keynum
      // is equal to previous keynum
      if (voices[0] == keyNum && this.sustainPedalIsOn.not) {
        voices[1].release();
        voices = [];
      }
    }
    // polyphonic and retrigger monophonic (\normal monophonicMode)
    {this.sustainPedalIsOn} {
      forBy(1, voices.size, 2) {arg i;
        voicesToReleaseOnSustainOff = voicesToReleaseOnSustainOff.add(voices[i]);
      }
    }
    {this.sustainPedalIsOn.not} {
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

  sustainPedalIsOn_ {arg newValue;
    sustainPedalIsOn = newValue;

    if (sustainPedalIsOn.not) {
      voicesToReleaseOnSustainOff.do({arg voice;
        voice.release();
      });

      voicesToReleaseOnSustainOff = [];
    }
  }

  pitchBend {arg value;
    var pbMidiValue = value.linlin(0, 127, pitchBendSemitones.neg, pitchBendSemitones);
    if (value == 64) {pbMidiValue = 0};
    pbMidiValue.postln;

    forBy (0, voices.size, 2) {arg i;
      var keyNum = voices[i];
      keyNum!?{
        var voice = voices[i+1];
        var freq = (keyNum + pbMidiValue).midicps;

        voice.set(\freq, freq);
      };
    };
  }

  getVoice {arg keyNum;
    var numIndex = voices.indexOf(keyNum);
    ^voices[numIndex+1];
  }

  releaseAll {
    forBy (1, voices.size, 2) {arg i; 
      voices[i].release();
    };
  }
}
