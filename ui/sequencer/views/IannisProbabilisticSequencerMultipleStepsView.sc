IannisProbabilisticSequencerMultipleStepsView : ScrollView {
	var data, label;

	*new {arg numberOfSteps, name;
		^super.new.init(numberOfSteps, name);
	}

	init {arg numberOfSteps, name;
		label = StaticText.new;
		label.string = name;

		this.canvas = CompositeView();
		this.canvas.background = Color.gray(0.79, 1);

		this.hasHorizontalScroller = false;
		this.hasVerticalScroller = true;
		this.hasBorder = false;
		this.autohidesScrollers = false;

		data = Array.fill(128, {
			var dict = Dictionary.new;
			dict[\expression] = "60";
			dict[\probability] = 0;
		});

		this.updateSteps(numberOfSteps);

		this.minHeight = 200;

		this.layout = VLayout(label, nil);

	}

	updateSteps {arg numberOfSteps;
		// initialize sliders
		this.canvas.layout = GridLayout.rows(
			Array.fill(numberOfSteps, {arg n;
				var ch = IannisProbabilisticSequencerStepView.new(n+1);

				if(data[n].notNil, {
					ch.probabilitySlider.valueAction = data[n][\probability];
					ch.expressionField.valueAction = data[n][\expression];
				});

				// return
				ch;
			});
		);
	}

	updateData {
		canvas.children.do({arg item;
			if(item.isKindOf(IannisProbabilisticSequencerStepView), {
				var n = item.sliderLabel.string.asInt - 1;
				data[n][\probability] = item.probabilitySlider.value;
				data[n][\expression] = item.expressionField.string;
			});
		});
	}

}