(
// For development only
// ----------------------------------------------------------
LanguageConfig.removeIncludePath(Document.current.dir);
LanguageConfig.addIncludePath(Document.current.dir);
LanguageConfig.store;
thisProcess.recompile;
// ----------------------------------------------------------
)


w = Window.new;
a = ATProbabilisticSequencerView(\sequencer, 16);
w.layout = HLayout(a);

w.front;

(
//
// GUI
//
var window, knobA, patFunc, previousValue;
window = Window.new;
knobA = Knob.new(window);
knobA.mode = \vert;

patFunc = {arg seqName, prob, seqLength;
	var pat = Pbind(
		\midinote, Pwrand(
			// здесь должно быть определенное количество ступеней, как
			// в обычном секвенсере и будет определяться, какие на них ноты
			// но этим ступеням будет соответствовать вероятность
			// применяться она должна после того, как поднимиться кнопка мыши
			// то есть не при каждом изменении, тогда получится круто
			// вероятность может быть равной 0

			// в ритме вероятность ритмических фигур определяется

			// также нужен параметр — транспозиция

			[48, 60, 64, 67, 71],
			[prob, 0.5,  0.5, 0.5,  0.5].normalizeSum,
			inf),
		\dur, Pwrand([
			Pseq([0.25, 0.25]),
			Pseq([1.5, 0.5]),
			Pseq([1])
		], [4, 1, 1].normalizeSum, inf)
	);

	Pdef(seqName,
		// второе число можно сделать очень длинным, тогда будет иллюзия
		// постоянного рандома
		Pn(Pfindur(seqLength, Pseed(rrand(1, 10000), pat)), inf)
	);
};

previousValue = knobA.value;
knobA.mouseUpAction = {arg knob;
	if(previousValue != knob.value, {
		patFunc.value(\sequencer, knob.value, 8);
	});
	previousValue = knob.value;
};

window.front;

TempoClock.default.tempo = 117/60;

Pdef('sequencer').play;
// Pdef('sequencer2').play;
)