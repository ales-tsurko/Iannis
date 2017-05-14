(
// ---------------------------------------------------------
Quarks.install("~/Desktop/Iannis".standardizePath);
Quarks.uninstall("Iannis");
// ---------------------------------------------------------
)

(
  s.waitForBoot({
    SynthDef.synthDefDir = "~/Desktop/Iannis/synthdefs".standardizePath;

    s.loadDirectory(SynthDef.synthDefDir);

    s.sync;

    SynthDescLib(\iannis_synth);
    // init synthdescs for iannis' synthdefs
    SynthDescLib.getLib(\iannis_synth).read(SynthDef.synthDefDir+/+"granulator.scsyndef");

    // load iannis synthdesc lib into global lib
    SynthDescLib.getLib(\iannis_synth).synthDescs.do({|item| SynthDescLib.global.add(item)});
  });
)


// Sequencer
TempoClock.default.tempo = 107/60;

IannisProbabilisticSequencerView(\sequencer, \granulator, patternLength: 4).front;

// Synth view controller
(
g = IannisNodeGroup();
v = IannisSynthViewController(g, \granulator).front();
c = ScrollView();
m = IannisSynthMapParameter(\pos, "Position", v);
c.canvas = m;
c.front();
)


IannisAceWrapper().front;
IannisAceWrapper.clearCache();

a = NodeProxy(s, 1);

p = g.getState[\pos]; // hold source value

a.source = {SinOsc.kr(1, 0, 1).range(0.1, 0.6)};
a.fadeTime = 3;

g.set(\pos, a.bus.asMap);

a.clear(4);

// return to the source value
g.set(\pos, p);

// Tabbed view

l = StaticText();
l.string = "Hello";

v = CompositeView();
v.fixedHeight = 300;
v.fixedWidth = 200;

v.layout = HLayout(l);

t = IannisTabbedView("tab", v);

t.front();

// adding a tab
m = StaticText();
m.string = "another tab 2";

z = CompositeView();
z.fixedHeight = 300;
z.fixedWidth = 200;

z.layout = HLayout(m);

t.addPage("tab 2", z);

