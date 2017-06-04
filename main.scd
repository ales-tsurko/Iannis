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

    // survive CmdPeriod
    TempoClock.default.permanent = true;
  });
)


// Sequencer
TempoClock.default.tempo = 107/60;

IannisProbabilisticSequencerView(\sequencer, \granulator, patternLength: 4).front;

// Synth view controller
(
g = IannisNodeGroup();
v = IannisSynthViewController(g, \granulator).front();
)

p = ProxySpace(Server.default);

p.put(0, {(2+2).postln});
p.put(1, {SinOsc.ar*0.3!2});
p[1].stop

p.clear;
p.pop;

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

