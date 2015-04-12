/* Resource file for Explore */

#include	"Types.r"

/* You Win Alert */
resource 'ALRT' (128, preload)
{
	{40, 20, 160, 292}, 128,
	{
		OK, visible, sound1;
		OK, visible, sound1;
		OK, visible, sound1;
		OK, visible, sound1
	};
};

resource 'DITL' (128)
{
	 {
		{88, 180, 108, 260},
		Button
		{
			enabled,	"OK"
		};
		{8, 8, 24, 214},
		StaticText
		{
			disabled,	"You win!"
		};
	}
};

/* You Die Alert */
resource 'ALRT' (129, preload)
{
	{40, 20, 160, 292}, 129,
	{
		OK, visible, sound1;
		OK, visible, sound1;
		OK, visible, sound1;
		OK, visible, sound1
	};
};

resource 'DITL' (129)
{
	 {
		{88, 180, 108, 260},
		Button
		{
			enabled,	"OK"
		};
		{8, 8, 24, 214},
		StaticText
		{
			disabled,	"You are dead."
		};
	}
};

type 'EXPL' as 'STR ';

resource 'EXPL' (0)
{
	"Explore -- ver 4.4C"
};

data 'ICN#' (128)
{
	$"00 04 40 00 00 06 40 00 00 05 40 00 00 04 C0 00"/* ..@...@...@...À. */
	$"00 04 40 00 00 04 40 00 00 04 40 00 00 00 00 00"/* ..@...@...@..... */
	$"00 01 00 00 00 01 00 00 00 01 00 00 00 03 80 00"/* ..............€. */
	$"88 03 80 3E 88 03 80 20 A8 07 C0 20 A8 0F E0 3C"/* ˆ.€>ˆ.€ ¨.À ¨..< */
	$"50 04 40 20 50 02 80 20 50 02 80 3E 00 02 80 00"/* P.@ P.€ P.€>..€. */
	$"00 01 00 00 00 01 00 00 00 01 00 00 00 00 00 00"/* ................ */
	$"00 03 80 00 00 04 40 00 00 04 00 00 00 03 80 00"/* ..€...@.......€. */
	$"00 00 40 00 00 04 40 00 00 03 80 00 00 00 00 00"/* ..@...@...€..... */
	$"FF FF FF FE FF FF FF FE FF FF FF FE FF FF FF FE"/* ................ */
	$"FF FF FF FE FF FF FF FE FF FF FF FE FF FF FF FE"/* ................ */
	$"FF FF FF FE FF FF FF FE FF FF FF FE FF FF FF FE"/* ................ */
	$"FF FF FF FE FF FF FF FE FF FF FF FE FF FF FF FE"/* ................ */
	$"FF FF FF FE FF FF FF FE FF FF FF FE FF FF FF FE"/* ................ */
	$"FF FF FF FE FF FF FF FE FF FF FF FE FF FF FF FE"/* ................ */
	$"FF FF FF FE FF FF FF FE FF FF FF FE FF FF FF FE"/* ................ */
	$"FF FF FF FE FF FF FF FE FF FF FF FE 00 00 00 00"/* ................ */
};

data 'ICN#' (129)
{
	$"0F FF FC 00 08 00 06 00 08 04 45 00 08 06 44 80"/* ..........E...D€ */
	$"08 05 44 40 08 04 C4 20 08 04 47 F0 08 04 40 10"/* ..D@..Ä ..G...@. */
	$"08 04 40 10 08 00 00 10 08 01 00 10 08 01 00 10"/* ..@............. */
	$"08 01 00 10 08 03 80 10 08 03 80 10 08 03 80 10"/* ......€...€...€. */
	$"08 07 C0 10 08 0F E0 10 08 04 40 10 08 02 80 10"/* ..À.......@...€. */
	$"08 02 80 10 08 02 80 10 08 01 00 10 08 01 00 10"/* ..€...€......... */
	$"08 01 00 10 08 00 00 10 08 00 00 10 08 00 00 10"/* ................ */
	$"08 00 00 10 08 00 00 10 08 00 00 10 0F FF FF F0"/* ................ */
	$"0F FF FC 00 0F FF FE 00 0F FF FF 00 0F FF FF 80"/* ...............€ */
	$"0F FF FF C0 0F FF FF E0 0F FF FF F0 0F FF FF F0"/* ...À............ */
	$"0F FF FF F0 0F FF FF F0 0F FF FF F0 0F FF FF F0"/* ................ */
	$"0F FF FF F0 0F FF FF F0 0F FF FF F0 0F FF FF F0"/* ................ */
	$"0F FF FF F0 0F FF FF F0 0F FF FF F0 0F FF FF F0"/* ................ */
	$"0F FF FF F0 0F FF FF F0 0F FF FF F0 0F FF FF F0"/* ................ */
	$"0F FF FF F0 0F FF FF F0 0F FF FF F0 0F FF FF F0"/* ................ */
	$"0F FF FF F0 0F FF FF F0 0F FF FF F0 0F FF FF F0"/* ................ */
};

data 'ICN#' (130)
{
	$"00 7F FC 00 00 80 02 00 01 04 41 00 02 06 40 80"/* .....€....A...@€ */
	$"04 05 40 40 08 04 C0 20 10 04 40 10 20 04 40 08"/* ..@@..À ..@. .@. */
	$"40 04 40 04 80 00 00 02 80 01 00 02 80 01 00 02"/* @.@.€...€...€... */
	$"80 01 00 02 80 03 80 02 80 03 80 02 80 03 80 02"/* €...€.€.€.€.€.€. */
	$"80 07 C0 02 80 0F E0 02 80 04 40 02 80 02 80 02"/* €.À.€...€.@.€.€. */
	$"80 02 80 02 80 02 80 02 40 01 00 04 20 01 00 08"/* €.€.€.€.@... ... */
	$"10 01 00 10 08 00 00 20 04 00 00 40 02 00 00 80"/* ....... ...@...€ */
	$"01 00 01 00 00 80 02 00 00 7F FC 00 00 00 00 00"/* .....€.......... */
	$"00 7F FC 00 00 FF FE 00 01 FF FF 00 03 FF FF 80"/* ...............€ */
	$"07 FF FF C0 0F FF FF E0 1F FF FF F0 3F FF FF F8"/* ...À........?... */
	$"7F FF FF FC FF FF FF FE FF FF FF FE FF FF FF FE"/* ................ */
	$"FF FF FF FE FF FF FF FE FF FF FF FE FF FF FF FE"/* ................ */
	$"FF FF FF FE FF FF FF FE FF FF FF FE FF FF FF FE"/* ................ */
	$"FF FF FF FE FF FF FF FE 7F FF FF FC 3F FF FF F8"/* ............?... */
	$"1F FF FF F0 0F FF FF E0 07 FF FF C0 03 FF FF 80"/* ...........À...€ */
	$"01 FF FF 00 00 FF FE 00 00 7F FC 00 00 00 00 00"/* ................ */
};

resource 'FREF' (128)
{
	'APPL',
	0,
	""
};

resource 'FREF' (129)
{
	'EXPA',
	1,
	""
};

resource 'FREF' (130)
{
	'EXPS',
	2,
	""
};

resource 'BNDL' (128)
{
	'EXPL',
	0,
	{
		'ICN#',
		{
			0, 128,
			1, 129,
			2, 130
		},
		'FREF',
		{
			0, 128,
			1, 129,
			2, 130
		}
	}
};
