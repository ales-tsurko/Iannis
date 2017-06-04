IannisVoicesManager {
  var <>allowedNumberOfVoices,
  voices;

  *new {arg numberOfVoices = 4;
    ^super.new.init(numberOfVoices);
  }

  init {arg voicesNum;
    allowedNumberOfVoices = voicesNum;
    voices = [];
  }

  releaseAll {
    voices.do({arg item, n;
      if (n.odd) {
        item.release();
      }
    });
  }

  addVoice {arg keyNum, synth;
    voices = voices.addAll([keyNum, synth]);

    if (voices.size > (allowedNumberOfVoices*2)) {
      // remove keynum
      voices.removeAt(0);
      // free the voice and remove it
      voices[0].free();
      voices.removeAt(0);
    }
  }

  releaseVoice {arg keyNum;
    var numIndex = voices.indexOf(keyNum);
    numIndex!?{
      // remove keyNum
      voices.removeAt(numIndex);

      // release associated voice
      voices[numIndex].release();
      voices.removeAt(numIndex);
    }
  }

  getVoice {arg keyNum;
    var numIndex = voices.indexOf(keyNum);
    ^voices[numIndex+1];
  }
}
