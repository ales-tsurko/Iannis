IannisProbabilisticSequencerParametersView : CompositeView {
	var playStopButton, resetButton, updateButton,
	patternLengthField, seedField, correspondingSequencer,
  rootPopup, scalePopup, tuningPopup;

	*new {arg sequencer;
		^super.new.init(sequencer);
	}

	init {arg sequencer;
		var patternLengthLabel = StaticText.new;
		var seedLabel = StaticText.new;
    var rootLabel = StaticText.new;
    var scaleLabel = StaticText.new;
    var tuningLabel = StaticText.new;
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
      var newScale = Scale.newFromKey(Scale.names[popup.value]);
      correspondingSequencer.scale = newScale;
    };

    scalePopup.valueAction = 11; // index of chromatic scale

    // tuning
    tuningLabel.string = "Tuning:";
    tuningPopup = PopUpMenu.new;
    tuningPopup.items = [];


    Tuning.names.do({arg name;
      var newTuning = Tuning.at(name).name;
      tuningPopup.items = tuningPopup.items.add(newTuning);
    });

    tuningPopup.action = {arg popup;
      var newTuning = Tuning.names[popup.value];
      correspondingSequencer.scale.tuning = newTuning;
    };
    
    tuningPopup.valueAction = 3; // index of ET12

		// pattern length
		patternLengthLabel.string = "Pattern length (beats):";
		patternLengthField = TextField.new;
		patternLengthField.fixedWidth = 125;
		patternLengthField.action = {arg field;
        this.lengthFieldAction(field);
		};

		patternLengthField.valueAction = 8;

		// seed
		seedLabel.string = "Version number:";
		seedField = TextField.new;
		seedField.fixedWidth = 100;

		seedField.action = {arg field;
        this.seedFieldAction(field);
		};
    		
    seedField.valueAction = correspondingSequencer.seed;


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
		updateButton.states = [["Random Version"]];
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
          nil, 
          rootLabel, rootPopup, 
          scaleLabel, scalePopup
        ),
        HLayout(
          nil, tuningLabel, tuningPopup 
        )
      )
    );

		this.fixedHeight = 75;
	}

  lengthFieldAction {arg field;
    var expr = field.value.interpret;
    // if there is no anything except spaces -- assign 4
    if(field.value.findRegexp("[^ \t]").size == 0, {
      field.value = 4;
      expr = 4;
    });

    if(expr.notNil, {
      correspondingSequencer.length = expr;
    });
  }

  seedFieldAction {arg field;
    var expr = field.value.interpret;
    // if there is no anything except spaces -- assign 4
    if(field.value.findRegexp("[^ \t]").size == 0, {
      field.value = correspondingSequencer.seed;
      expr = correspondingSequencer.seed;
    });

    if(expr.notNil, {
      correspondingSequencer.seed = expr;
    });
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
