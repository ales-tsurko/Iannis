IannisProbabilisticSequencerMultipleStepsView : ScrollView {
	var data, label, <sequencer, <correspondingKey, <>transposition, <numOfSteps;

	*new {arg sequencer, numberOfSteps, name, key;
		^super.new.init(sequencer, numberOfSteps, name, key);
	}

	init {arg correspondingSequencer, numberOfSteps, name, key;
		correspondingKey = key;
		sequencer = correspondingSequencer;
    numOfSteps = numberOfSteps;

		label = StaticText.new;
		label.string = name;

		this.canvas = CompositeView();

		this.hasHorizontalScroller = false;
		this.hasVerticalScroller = true;
		this.hasBorder = false;
		this.autohidesScrollers = false;

		data = Dictionary.new;
		data[\expression] = [];
		data[\realExpression] = [];
		data[\probability] = [];
		128.do({arg n;
			data[\expression] = data[\expression].add("");
			data[\realExpression] = data[\realExpression].add(nil);
			data[\probability] = data[\probability].add(0);
		});

		this.updateSteps(numberOfSteps);

		this.minHeight = 200;
		this.minWidth = 940;

		this.layout = VLayout(label, nil);

	}

	updateSteps {arg numberOfSteps;
    numOfSteps = numberOfSteps;
		this.canvas.layout = GridLayout();
		this.canvas.layout.vSpacing = 30;

		numberOfSteps.do({arg n;
			var ch = IannisProbabilisticSequencerStepView.new(n+1, n, this);

			ch.probabilitySlider.valueAction = data[\probability][n];
			ch.expressionField.valueAction = data[\expression][n];

			this.canvas.layout.add(ch, floor(n/4), n%4)
		});

    this.stepAction();
	}

	updateData {
		canvas.children.do({arg item;
			if(item.isKindOf(IannisProbabilisticSequencerStepView), {
				var n = item.number;
				data[\probability][n] = item.probabilitySlider.value;
				data[\expression][n] = item.expressionField.string;
				data[\realExpression][n] = item.realExpression;
			});
		});
	}

	stepAction {arg stepView;
		var values;

		// if argument is passed -- update the data
		if(stepView.notNil, {
			var n = stepView.number;
			data[\probability][n] = stepView.probabilitySlider.value;
			data[\expression][n] = stepView.expressionField.string;
			data[\realExpression][n] = stepView.realExpression;
		});

		sequencer.updateEvent(correspondingKey, data[\realExpression], data[\probability], transposition, numOfSteps);
	}

}
