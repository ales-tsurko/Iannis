IannisProbabilisticSequencerParametersView : CompositeView {
	var playStopButton, resetButton, updateButton,
	patternLengthField, seedField, correspondingSequencer,
  rootPopup, scalePopup;

	*new {arg sequencer;
		^super.new.init(sequencer);
	}

	init {arg sequencer;
		var patternLengthLabel = StaticText.new;
		var seedLabel = StaticText.new;
    var rootLabel = StaticText.new;
    var scaleLabel = StaticText.new;
    var beatCounter = StaticText.new;
    var pitches = ["G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#"];
    correspondingSequencer = sequencer;

    // beat counter
    beatCounter.string = (correspondingSequencer.time+1).asString;
    beatCounter.font = Font("Arial", 42);
    correspondingSequencer.timeAction = {arg newTime;
      beatCounter.string = newTime.asString;
    };

    // root
    rootLabel.string = "Root:";
    rootPopup = PopUpMenu.new;
    rootPopup.items = [];

    128.do({arg i;
      var newPitch = pitches[i%11]++(i/12).floor;
      rootPopup.items = rootPopup.items.add(newPitch);
    });

    rootPopup.action = {arg popup;
      correspondingSequencer.root = popup.value - 60;
    };

    rootPopup.valueAction = 60;

    // scale
    scaleLabel.string = "Scale:";
    scalePopup = PopUpMenu.new;
    scalePopup.items = [];

    Scale.names.do({arg name;
      var newScale = Scale.at(name).name;
      scalePopup.items = scalePopup.items.add(newScale);
    });

    scalePopup.action = {arg popup;
      var newScale = Scale.at(Scale.names[popup.value]);
      correspondingSequencer.scale = newScale;
    };

    scalePopup.valueAction = 11; // index of chromatic scale

		// pattern length
		patternLengthLabel.string = "Pattern length (beats):";
		patternLengthField = TextField.new;
		patternLengthField.fixedWidth = 50;

		patternLengthField.value = 8;

		patternLengthField.action = {arg field;
			this.lengthFieldAction(field);
		};

		patternLengthField.focusLostAction = {arg view;
			patternLengthField.doAction;
		};

		// seed
		seedLabel.string = "Seed:";
		seedField = TextField.new;
		seedField.fixedWidth = 100;
		seedField.value = sequencer.seed;

		seedField.action = {arg field;
			this.seedFieldAction(field);
		};

		seedField.focusLostAction = {arg view;
			seedField.doAction;
		};

		// play/stop button
		playStopButton = Button.new;
		playStopButton.states = [["Play"], ["Stop"]];
		playStopButton.fixedWidth = 200;

		playStopButton.action = {arg button;
			this.playStopButtonAction(button);
		};

		// reset button
		resetButton = Button.new;
		resetButton.states = [["Reset"]];
		resetButton.fixedWidth = 200;

		resetButton.action = {arg button;
			this.resetButtonAction(button);
		};

		// update button
		updateButton = Button.new;
		updateButton.states = [["Update"]];
		updateButton.fixedWidth = 200;

		updateButton.action = {arg button;
			this.updateButtonAction(button);
		};

    this.layout = HLayout(
      VLayout(
        HLayout(
          patternLengthLabel, patternLengthField,
          nil,
          seedLabel, seedField
        ),
        HLayout(
          playStopButton, resetButton, updateButton,
        )
      ),

      nil,
      beatCounter,
      nil,

      VLayout(
        HLayout(
          nil, scaleLabel, scalePopup
        ),
        HLayout(
          nil, rootLabel, rootPopup
        )
      )
    );

		this.fixedHeight = 75;
	}

	lengthFieldAction {arg field;
		var length = field.value.asInt.clip(1, 999);
		patternLengthField.value = length;

		correspondingSequencer.changeLength(length);
	}

	seedFieldAction {arg field;
		var seed = field.value.asInt;
		field.value = seed;

		correspondingSequencer.setSeed(seed);
	}

	playStopButtonAction {arg button;
		if(button.value == 1, {
			correspondingSequencer.play();
		}, {
			correspondingSequencer.stop();
		});
	}

	resetButtonAction {arg button;
		correspondingSequencer.reset();
	}

	updateButtonAction {arg button;
		correspondingSequencer.regenerate();

		// update seed field
		seedField.value = correspondingSequencer.seed;
	}
}
