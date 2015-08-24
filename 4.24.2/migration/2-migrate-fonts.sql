--
-- PostgreSQL script to wipe the IRIS font database and add the default IRIS
-- fonts (albeit renumbered) along with the 5x7 "VER-MAC Font #01" extracted
-- from Ver-Mac's Centralo application.
--
-- NOTE:
--   - PCMS-1210 DMS only seem to allow uploading fonts that are numbered
--     either "2" or "3" (presumably "1" is read-only, but v1 of NTCIP 1203
--     makes it difficult to know for sure).  For the purposes of better
--     compatibility with Centralo, the PDMS console, and possibly other
--     ATMS that may be using these devices, we will number this font "1".
--     Attempts to upload this font to these PDMS will result in an SNMP
--     GenErr, which IRIS will recognize.  Therefore, IRIS will not upload
--     this font to these DMS, and any local changes made to this font in
--     IRIS will only be reflected client-side.
--     I am currently investigating these issues further and plan to work
--     with MnDOT to better handle 1203v1 DMS with read-only fonts.
--
-- RELEVANT DATABASE TABLES:
--   FONT:    font definitions
--   GRAPHIC: named bitmaps for all fonts
--   GLYPH:   named mappings of font:codepoint tuples to all graphics
--

SET client_encoding = 'UTF8';

\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

-- clear out reference dependencies
UPDATE iris._dms SET default_font=NULL;

-- wipe font tables
DELETE from iris.glyph;
DELETE from iris.graphic;
DELETE from iris.font;

-- load iris.font
COPY iris.font (name, f_number, height, width, line_spacing, char_spacing, version_id) FROM stdin;
Ver-Mac 5x7	1	7	5	1	1	0
07_char	11	7	5	0	0	7314
07_line	12	7	0	0	2	40473
08_full	13	8	0	2	2	0
09_full	14	9	0	2	2	0
10_full	15	10	0	3	2	0
11_full	16	11	0	3	2	0
12_full	17	12	0	3	2	0
12_full_bold	18	12	0	4	3	0
13_full	19	13	0	4	3	0
14_full	21	14	0	4	3	0
16_full	23	16	0	4	3	0
18_full	24	18	0	5	3	0
20_full	25	20	0	5	3	0
24_full	26	24	0	5	4	0
_09_full_12	27	12	0	2	2	0
_7_full	28	7	0	3	2	0
\.

-- load iris.graphic
COPY iris.graphic (name, g_number, bpp, height, width, pixels) FROM stdin;
Ver-Mac 5x7_32	\N	1	7	5	AAAAAAA=
Ver-Mac 5x7_33	\N	1	7	5	IQhCAIA=
Ver-Mac 5x7_34	\N	1	7	5	UpQAAAA=
Ver-Mac 5x7_35	\N	1	7	5	Ur6vqUA=
Ver-Mac 5x7_36	\N	1	7	5	I+ji+IA=
Ver-Mac 5x7_37	\N	1	7	5	xkRETGA=
Ver-Mac 5x7_38	\N	1	7	5	ZKiKyaA=
Ver-Mac 5x7_39	\N	1	7	5	YRAAAAA=
Ver-Mac 5x7_40	\N	1	7	5	ERCEEEA=
Ver-Mac 5x7_41	\N	1	7	5	QQQhEQA=
Ver-Mac 5x7_42	\N	1	7	5	JV33VIA=
Ver-Mac 5x7_43	\N	1	7	5	AQnyEAA=
Ver-Mac 5x7_44	\N	1	7	5	AAAGEQA=
Ver-Mac 5x7_45	\N	1	7	5	AAHwAAA=
Ver-Mac 5x7_46	\N	1	7	5	AAAAMYA=
Ver-Mac 5x7_47	\N	1	7	5	AEREQAA=
Ver-Mac 5x7_48	\N	1	7	5	dGdcxcA=
Ver-Mac 5x7_49	\N	1	7	5	IwhCEcA=
Ver-Mac 5x7_50	\N	1	7	5	dELoQ+A=
Ver-Mac 5x7_51	\N	1	7	5	dEJgxcA=
Ver-Mac 5x7_52	\N	1	7	5	EZUviEA=
Ver-Mac 5x7_53	\N	1	7	5	/CHgxcA=
Ver-Mac 5x7_54	\N	1	7	5	dGHoxcA=
Ver-Mac 5x7_55	\N	1	7	5	+EREIQA=
Ver-Mac 5x7_56	\N	1	7	5	dGLoxcA=
Ver-Mac 5x7_57	\N	1	7	5	dGLwiYA=
Ver-Mac 5x7_58	\N	1	7	5	AxgGMAA=
Ver-Mac 5x7_59	\N	1	7	5	AxgGEQA=
Ver-Mac 5x7_60	\N	1	7	5	EREEEEA=
Ver-Mac 5x7_61	\N	1	7	5	AD4PgAA=
Ver-Mac 5x7_62	\N	1	7	5	gggiIgA=
Ver-Mac 5x7_63	\N	1	7	5	dEIiAIA=
Ver-Mac 5x7_64	\N	1	7	5	dELa1cA=
Ver-Mac 5x7_65	\N	1	7	5	dGP4xiA=
Ver-Mac 5x7_66	\N	1	7	5	9GPox8A=
Ver-Mac 5x7_67	\N	1	7	5	dGEIRcA=
Ver-Mac 5x7_68	\N	1	7	5	5KMYy4A=
Ver-Mac 5x7_69	\N	1	7	5	/CHoQ+A=
Ver-Mac 5x7_70	\N	1	7	5	/CHoQgA=
Ver-Mac 5x7_71	\N	1	7	5	dGF4xeA=
Ver-Mac 5x7_72	\N	1	7	5	jGP4xiA=
Ver-Mac 5x7_73	\N	1	7	5	cQhCEcA=
Ver-Mac 5x7_74	\N	1	7	5	OIQhSYA=
Ver-Mac 5x7_75	\N	1	7	5	jKmKSiA=
Ver-Mac 5x7_76	\N	1	7	5	hCEIQ+A=
Ver-Mac 5x7_77	\N	1	7	5	jusYxiA=
Ver-Mac 5x7_78	\N	1	7	5	jHNZxiA=
Ver-Mac 5x7_79	\N	1	7	5	dGMYxcA=
Ver-Mac 5x7_80	\N	1	7	5	9GPoQgA=
Ver-Mac 5x7_81	\N	1	7	5	dGMayaA=
Ver-Mac 5x7_82	\N	1	7	5	9GPqSiA=
Ver-Mac 5x7_83	\N	1	7	5	dGDgxcA=
Ver-Mac 5x7_84	\N	1	7	5	+QhCEIA=
Ver-Mac 5x7_85	\N	1	7	5	jGMYxcA=
Ver-Mac 5x7_86	\N	1	7	5	jGMYqIA=
Ver-Mac 5x7_87	\N	1	7	5	jGNa1UA=
Ver-Mac 5x7_88	\N	1	7	5	jFRFRiA=
Ver-Mac 5x7_89	\N	1	7	5	jGKiEIA=
Ver-Mac 5x7_90	\N	1	7	5	+EREQ+A=
Ver-Mac 5x7_91	\N	1	7	5	5CEIQ4A=
Ver-Mac 5x7_92	\N	1	7	5	BBBBBAA=
Ver-Mac 5x7_93	\N	1	7	5	OEIQhOA=
Ver-Mac 5x7_94	\N	1	7	5	IoAAAAA=
Ver-Mac 5x7_95	\N	1	7	5	AAAAA+A=
Ver-Mac 5x7_96	\N	1	7	5	QQAAAAA=
Ver-Mac 5x7_97	\N	1	7	5	ABwXxeA=
Ver-Mac 5x7_98	\N	1	7	5	hCHox8A=
Ver-Mac 5x7_99	\N	1	7	5	AB0YRcA=
Ver-Mac 5x7_100	\N	1	7	5	CEL4xeA=
Ver-Mac 5x7_101	\N	1	7	5	AB0fwcA=
Ver-Mac 5x7_102	\N	1	7	5	MlHkIQA=
Ver-Mac 5x7_103	\N	1	7	5	AB8Xh+A=
Ver-Mac 5x7_104	\N	1	7	5	hCHoxiA=
Ver-Mac 5x7_105	\N	1	7	5	IAhCEIA=
Ver-Mac 5x7_106	\N	1	7	5	EAQhSYA=
Ver-Mac 5x7_107	\N	1	7	5	hKmKSiA=
Ver-Mac 5x7_108	\N	1	7	5	IQhCEIA=
Ver-Mac 5x7_109	\N	1	7	5	ADdYxiA=
Ver-Mac 5x7_110	\N	1	7	5	AD0YxiA=
Ver-Mac 5x7_111	\N	1	7	5	AB0YxcA=
Ver-Mac 5x7_112	\N	1	7	5	9GPoQgA=
Ver-Mac 5x7_113	\N	1	7	5	fGLwhCA=
Ver-Mac 5x7_114	\N	1	7	5	AD0YQgA=
Ver-Mac 5x7_115	\N	1	7	5	AB8HB+A=
Ver-Mac 5x7_116	\N	1	7	5	IT5CEIA=
Ver-Mac 5x7_117	\N	1	7	5	ACMYx+A=
Ver-Mac 5x7_118	\N	1	7	5	ACMYqIA=
Ver-Mac 5x7_119	\N	1	7	5	ACMa1UA=
Ver-Mac 5x7_120	\N	1	7	5	ACMXRiA=
Ver-Mac 5x7_121	\N	1	7	5	ACMXh8A=
Ver-Mac 5x7_122	\N	1	7	5	AD4XQ+A=
Ver-Mac 5x7_123	\N	1	7	5	IhEEIIA=
Ver-Mac 5x7_124	\N	1	7	5	IQhCEIA=
Ver-Mac 5x7_125	\N	1	7	5	IIQRCIA=
Ver-Mac 5x7_126	\N	1	7	5	ABVAAAA=
07_char_32	\N	1	7	5	AAAAAAA=
07_char_33	\N	1	7	5	IQhCAIA=
07_char_34	\N	1	7	5	UoAAAAA=
07_char_35	\N	1	7	5	Ur6vqUA=
07_char_36	\N	1	7	5	I6jiuIA=
07_char_37	\N	1	7	5	xkRETGA=
07_char_38	\N	1	7	5	RSiKyaA=
07_char_39	\N	1	7	5	IQAAAAA=
07_char_40	\N	1	7	5	ERCEEEA=
07_char_41	\N	1	7	5	QQQhEQA=
07_char_42	\N	1	7	5	JVxHVIA=
07_char_43	\N	1	7	5	AQnyEAA=
07_char_44	\N	1	7	5	AAAAEQA=
07_char_45	\N	1	7	5	AADgAAA=
07_char_46	\N	1	7	5	AAAAAIA=
07_char_47	\N	1	7	5	AEREQAA=
07_char_48	\N	1	7	5	MlKUpMA=
07_char_49	\N	1	7	5	IwhCEcA=
07_char_50	\N	1	7	5	dEJkQ+A=
07_char_51	\N	1	7	5	dEJgxcA=
07_char_52	\N	1	7	5	EZUviEA=
07_char_53	\N	1	7	5	/CDgxcA=
07_char_54	\N	1	7	5	dGHoxcA=
07_char_55	\N	1	7	5	+EQiEQA=
07_char_56	\N	1	7	5	dGLoxcA=
07_char_57	\N	1	7	5	dGLwxcA=
07_char_58	\N	1	7	5	ABAEAAA=
07_char_59	\N	1	7	5	AAgCIAA=
07_char_60	\N	1	7	5	EREEEEA=
07_char_61	\N	1	7	5	AD4PgAA=
07_char_62	\N	1	7	5	QQQREQA=
07_char_63	\N	1	7	5	ZIRCAIA=
07_char_64	\N	1	7	5	dGdZwcA=
07_char_65	\N	1	7	5	dGP4xiA=
07_char_66	\N	1	7	5	9GPox8A=
07_char_67	\N	1	7	5	dGEIRcA=
07_char_68	\N	1	7	5	9GMYx8A=
07_char_69	\N	1	7	5	/CHoQ+A=
07_char_70	\N	1	7	5	/CHoQgA=
07_char_71	\N	1	7	5	dGF4xeA=
07_char_72	\N	1	7	5	jGP4xiA=
07_char_73	\N	1	7	5	cQhCEcA=
07_char_74	\N	1	7	5	EIQhSYA=
07_char_75	\N	1	7	5	jKmKSiA=
07_char_76	\N	1	7	5	hCEIQ+A=
07_char_77	\N	1	7	5	jusYxiA=
07_char_78	\N	1	7	5	jnNZziA=
07_char_79	\N	1	7	5	dGMYxcA=
07_char_80	\N	1	7	5	9GPoQgA=
07_char_81	\N	1	7	5	dGMayaA=
07_char_82	\N	1	7	5	9GPqSiA=
07_char_83	\N	1	7	5	dGDgxcA=
07_char_84	\N	1	7	5	+QhCEIA=
07_char_85	\N	1	7	5	jGMYxcA=
07_char_86	\N	1	7	5	jGMVKIA=
07_char_87	\N	1	7	5	jGMa1UA=
07_char_88	\N	1	7	5	jFRFRiA=
07_char_89	\N	1	7	5	jFRCEIA=
07_char_90	\N	1	7	5	+EREQ+A=
07_char_91	\N	1	7	5	chCEIcA=
07_char_92	\N	1	7	5	BBBBBAA=
07_char_93	\N	1	7	5	cIQhCcA=
07_char_94	\N	1	7	5	IqIAAAA=
07_char_95	\N	1	7	5	AAAAA+A=
07_line_32	\N	1	7	1	AA==
07_line_33	\N	1	7	2	qog=
07_line_34	\N	1	7	3	tAAA
07_line_35	\N	1	7	5	Ur6vqUA=
07_line_36	\N	1	7	5	I6jiuIA=
07_line_37	\N	1	7	5	xkRETGA=
07_line_38	\N	1	7	5	RSiKyaA=
07_line_39	\N	1	7	1	wA==
07_line_40	\N	1	7	3	KkiI
07_line_41	\N	1	7	3	iJKg
07_line_42	\N	1	7	7	EFEUFEUEAA==
07_line_43	\N	1	7	5	AQnyEAA=
07_line_44	\N	1	7	3	AACg
07_line_45	\N	1	7	4	AA8AAA==
07_line_46	\N	1	7	2	AAg=
07_line_47	\N	1	7	5	AEREQAA=
07_line_48	\N	1	7	4	aZmZYA==
07_line_49	\N	1	7	3	WSS4
07_line_50	\N	1	7	4	aRJI8A==
07_line_51	\N	1	7	4	aRYZYA==
07_line_52	\N	1	7	5	EZUviEA=
07_line_53	\N	1	7	4	+IYZYA==
07_line_54	\N	1	7	4	aY6ZYA==
07_line_55	\N	1	7	4	8RIkQA==
07_line_56	\N	1	7	4	aZaZYA==
07_line_57	\N	1	7	4	aZcZYA==
07_line_58	\N	1	7	2	CIA=
07_line_59	\N	1	7	3	AQUA
07_line_60	\N	1	7	4	EkhCEA==
07_line_61	\N	1	7	4	APDwAA==
07_line_62	\N	1	7	4	hCEkgA==
07_line_63	\N	1	7	4	aRIgIA==
07_line_64	\N	1	7	5	dGdZwcA=
07_line_65	\N	1	7	4	aZ+ZkA==
07_line_66	\N	1	7	4	6Z6Z4A==
07_line_67	\N	1	7	4	aYiJYA==
07_line_68	\N	1	7	4	6ZmZ4A==
07_line_69	\N	1	7	4	+I6I8A==
07_line_70	\N	1	7	4	+I6IgA==
07_line_71	\N	1	7	4	aYuZcA==
07_line_72	\N	1	7	4	mZ+ZkA==
07_line_73	\N	1	7	3	6SS4
07_line_74	\N	1	7	4	EREZYA==
07_line_75	\N	1	7	4	maypkA==
07_line_76	\N	1	7	4	iIiI8A==
07_line_77	\N	1	7	7	g46smTBggA==
07_line_78	\N	1	7	5	jnNZziA=
07_line_79	\N	1	7	4	aZmZYA==
07_line_80	\N	1	7	4	6Z6IgA==
07_line_81	\N	1	7	5	dGMayaA=
07_line_82	\N	1	7	4	6Z6pkA==
07_line_83	\N	1	7	4	aYYZYA==
07_line_84	\N	1	7	5	+QhCEIA=
07_line_85	\N	1	7	4	mZmZYA==
07_line_86	\N	1	7	5	jGMVKIA=
07_line_87	\N	1	7	7	gwYMmrVRAA==
07_line_88	\N	1	7	5	jFRFRiA=
07_line_89	\N	1	7	5	jFRCEIA=
07_line_90	\N	1	7	5	+EREQ+A=
07_line_91	\N	1	7	3	8kk4
07_line_92	\N	1	7	5	BBBBBAA=
07_line_93	\N	1	7	3	5JJ4
07_line_94	\N	1	7	5	IqIAAAA=
07_line_95	\N	1	7	5	AAAAA+A=
08_full_32	\N	1	8	1	AA==
08_full_33	\N	1	8	2	qqI=
08_full_34	\N	1	8	3	tAAA
08_full_35	\N	1	8	5	Ur6vqUA=
08_full_36	\N	1	8	5	I6jiuIA=
08_full_37	\N	1	8	5	xkRETGA=
08_full_38	\N	1	8	5	RSiKym0=
08_full_39	\N	1	8	1	wA==
08_full_40	\N	1	8	3	KkkR
08_full_41	\N	1	8	3	iJJU
08_full_42	\N	1	8	5	ASrnVIA=
08_full_43	\N	1	8	5	AQnyEAA=
08_full_44	\N	1	8	3	AAAU
08_full_45	\N	1	8	4	AA8AAA==
08_full_46	\N	1	8	2	AAI=
08_full_47	\N	1	8	5	CERCIhA=
08_full_48	\N	1	8	4	aZmZlg==
08_full_49	\N	1	8	3	WSSX
08_full_50	\N	1	8	5	dEImQh8=
08_full_51	\N	1	8	5	dEJghi4=
08_full_52	\N	1	8	5	EZUviEI=
08_full_53	\N	1	8	5	/CDghi4=
08_full_54	\N	1	8	5	dGHoxi4=
08_full_55	\N	1	8	5	+EQiEQg=
08_full_56	\N	1	8	5	dGLoxi4=
08_full_57	\N	1	8	5	dGMXhi4=
08_full_58	\N	1	8	2	CIA=
08_full_59	\N	1	8	3	AQUA
08_full_60	\N	1	8	4	EkhCEA==
08_full_61	\N	1	8	4	APDwAA==
08_full_62	\N	1	8	4	hCEkgA==
08_full_63	\N	1	8	5	dEIiEAQ=
08_full_64	\N	1	8	5	dGdazg4=
08_full_65	\N	1	8	5	dGMfxjE=
08_full_66	\N	1	8	5	9GPoxj4=
08_full_67	\N	1	8	5	dGEIQi4=
08_full_68	\N	1	8	5	9GMYxj4=
08_full_69	\N	1	8	5	/CHIQh8=
08_full_70	\N	1	8	5	/CHIQhA=
08_full_71	\N	1	8	5	dGEJxi4=
08_full_72	\N	1	8	5	jGP4xjE=
08_full_73	\N	1	8	3	6SSX
08_full_74	\N	1	8	4	ERERlg==
08_full_75	\N	1	8	5	jKmKSjE=
08_full_76	\N	1	8	4	iIiIjw==
08_full_77	\N	1	8	7	g46smTBgwQ==
08_full_78	\N	1	8	5	jnNaznE=
08_full_79	\N	1	8	5	dGMYxi4=
08_full_80	\N	1	8	5	9GPoQhA=
08_full_81	\N	1	8	5	dGMY1k0=
08_full_82	\N	1	8	5	9GPqSjE=
08_full_83	\N	1	8	5	dGDghi4=
08_full_84	\N	1	8	5	+QhCEIQ=
08_full_85	\N	1	8	5	jGMYxi4=
08_full_86	\N	1	8	5	jGMYqUQ=
08_full_87	\N	1	8	7	gwYMGTVqog==
08_full_88	\N	1	8	5	jFRCKjE=
08_full_89	\N	1	8	5	jFSiEIQ=
08_full_90	\N	1	8	5	+ERCIh8=
08_full_91	\N	1	8	3	8kkn
08_full_92	\N	1	8	5	hBBCCCE=
08_full_93	\N	1	8	3	5JJP
08_full_94	\N	1	8	5	IqIAAAA=
08_full_95	\N	1	8	5	AAAAAB8=
09_full_32	\N	1	9	1	AAA=
09_full_33	\N	1	9	2	qqiA
09_full_34	\N	1	9	3	tAAAAA==
09_full_35	\N	1	9	5	Ur6lfUoA
09_full_36	\N	1	9	5	I6lHFK4g
09_full_37	\N	1	9	5	BjIiImMA
09_full_38	\N	1	9	6	IUUIYliidA==
09_full_39	\N	1	9	1	wAA=
09_full_40	\N	1	9	3	KkkiIA==
09_full_41	\N	1	9	3	iJJKgA==
09_full_42	\N	1	9	5	ASriOqQA
09_full_43	\N	1	9	5	AAhPkIAA
09_full_44	\N	1	9	3	AAACgA==
09_full_45	\N	1	9	4	AADwAAA=
09_full_46	\N	1	9	2	AACA
09_full_47	\N	1	9	5	CEQiIRCA
09_full_48	\N	1	9	5	dGMYxjFw
09_full_49	\N	1	9	3	WSSS4A==
09_full_50	\N	1	9	5	dEIiIhD4
09_full_51	\N	1	9	5	dEITBDFw
09_full_52	\N	1	9	5	EZUpfEIQ
09_full_53	\N	1	9	5	/CEPBDFw
09_full_54	\N	1	9	5	dGEPRjFw
09_full_55	\N	1	9	5	+EIhEIhA
09_full_56	\N	1	9	5	dGMXRjFw
09_full_57	\N	1	9	5	dGMXhDFw
09_full_58	\N	1	9	2	AiAA
09_full_59	\N	1	9	3	ACCgAA==
09_full_60	\N	1	9	4	ASSEIQA=
09_full_61	\N	1	9	4	AA8PAAA=
09_full_62	\N	1	9	4	CEISSAA=
09_full_63	\N	1	9	5	dEIREIAg
09_full_64	\N	1	9	6	ehlrppngeA==
09_full_65	\N	1	9	5	IqMfxjGI
09_full_66	\N	1	9	5	9GMfRjHw
09_full_67	\N	1	9	5	dGEIQhFw
09_full_68	\N	1	9	5	9GMYxjHw
09_full_69	\N	1	9	5	/CEOQhD4
09_full_70	\N	1	9	5	/CEOQhCA
09_full_71	\N	1	9	5	dGEJxjFw
09_full_72	\N	1	9	5	jGMfxjGI
09_full_73	\N	1	9	3	6SSS4A==
09_full_74	\N	1	9	4	ERERGWA=
09_full_75	\N	1	9	5	jGVMUlGI
09_full_76	\N	1	9	4	iIiIiPA=
09_full_77	\N	1	9	7	g46smTBgwYI=
09_full_78	\N	1	9	6	xxpplljjhA==
09_full_79	\N	1	9	5	dGMYxjFw
09_full_80	\N	1	9	5	9GMfQhCA
09_full_81	\N	1	9	5	dGMYxrJo
09_full_82	\N	1	9	5	9GMfUlGI
09_full_83	\N	1	9	5	dGEHBDFw
09_full_84	\N	1	9	5	+QhCEIQg
09_full_85	\N	1	9	5	jGMYxjFw
09_full_86	\N	1	9	5	jGMYqUQg
09_full_87	\N	1	9	7	gwYMGTJq1UQ=
09_full_88	\N	1	9	5	jGKiKjGI
09_full_89	\N	1	9	5	jGKiEIQg
09_full_90	\N	1	9	5	+EQiIRD4
09_full_91	\N	1	9	3	8kkk4A==
09_full_92	\N	1	9	5	hBCCCEEI
09_full_93	\N	1	9	3	5JJJ4A==
09_full_94	\N	1	9	5	IqIAAAAA
09_full_95	\N	1	9	5	AAAAAAD4
10_full_32	\N	1	10	1	AAA=
10_full_33	\N	1	10	2	qqgg
10_full_34	\N	1	10	3	tAAAAA==
10_full_35	\N	1	10	5	ApX1K+pQAA==
10_full_36	\N	1	10	5	I6lHFK4gAA==
10_full_37	\N	1	10	5	BjIiERMYAA==
10_full_38	\N	1	10	6	IUUIYliilZA=
10_full_39	\N	1	10	1	wAA=
10_full_40	\N	1	10	3	KkkkRA==
10_full_41	\N	1	10	3	iJJJUA==
10_full_42	\N	1	10	5	ASriOqQAAA==
10_full_43	\N	1	10	5	AAhPkIAAAA==
10_full_44	\N	1	10	3	AAACUA==
10_full_45	\N	1	10	4	AADwAAA=
10_full_46	\N	1	10	2	AACg
10_full_47	\N	1	10	5	CEQiEQiEAA==
10_full_48	\N	1	10	5	dGMYxjGLgA==
10_full_49	\N	1	10	3	WSSSXA==
10_full_50	\N	1	10	5	dGIRERCHwA==
10_full_51	\N	1	10	5	dEITBCGLgA==
10_full_52	\N	1	10	5	EZUpfEIQgA==
10_full_53	\N	1	10	5	/CEPBCGLgA==
10_full_54	\N	1	10	5	dGEPRjGLgA==
10_full_55	\N	1	10	5	+EIhEIhCAA==
10_full_56	\N	1	10	5	dGMXRjGLgA==
10_full_57	\N	1	10	5	dGMYvCGLgA==
10_full_58	\N	1	10	2	CgoA
10_full_59	\N	1	10	3	ASASgA==
10_full_60	\N	1	10	4	ASSEIQA=
10_full_61	\N	1	10	4	AA8PAAA=
10_full_62	\N	1	10	4	CEISSAA=
10_full_63	\N	1	10	5	dEIREIQBAA==
10_full_64	\N	1	10	6	ehlrpppngeA=
10_full_65	\N	1	10	5	IqMY/jGMQA==
10_full_66	\N	1	10	5	9GMfRjGPgA==
10_full_67	\N	1	10	5	dGEIQhCLgA==
10_full_68	\N	1	10	5	9GMYxjGPgA==
10_full_69	\N	1	10	5	/CEOQhCHwA==
10_full_70	\N	1	10	5	/CEOQhCEAA==
10_full_71	\N	1	10	5	dGEITjGLgA==
10_full_72	\N	1	10	5	jGMfxjGMQA==
10_full_73	\N	1	10	3	6SSSXA==
10_full_74	\N	1	10	4	EREREZY=
10_full_75	\N	1	10	5	jGVMUlGMQA==
10_full_76	\N	1	10	4	iIiIiI8=
10_full_77	\N	1	10	7	g46smTBgwYME
10_full_78	\N	1	10	6	hxxpplljjhA=
10_full_79	\N	1	10	5	dGMYxjGLgA==
10_full_80	\N	1	10	5	9GMfQhCEAA==
10_full_81	\N	1	10	5	dGMYxjWTQA==
10_full_82	\N	1	10	5	9GMfUlKMQA==
10_full_83	\N	1	10	5	dGEHBCGLgA==
10_full_84	\N	1	10	5	+QhCEIQhAA==
10_full_85	\N	1	10	5	jGMYxjGLgA==
10_full_86	\N	1	10	5	jGMYxUohAA==
10_full_87	\N	1	10	7	gwYMGDJk1aqI
10_full_88	\N	1	10	5	jGKiKjGMQA==
10_full_89	\N	1	10	5	jGMVEIQhAA==
10_full_90	\N	1	10	5	+EQiEQiHwA==
10_full_91	\N	1	10	3	8kkknA==
10_full_92	\N	1	10	5	hBCCEEIIQA==
10_full_93	\N	1	10	3	5JJJPA==
10_full_94	\N	1	10	5	IqIAAAAAAA==
10_full_95	\N	1	10	5	AAAAAAAHwA==
11_full_32	\N	1	11	1	AAA=
11_full_33	\N	1	11	2	qqoI
11_full_34	\N	1	11	3	toAAAAA=
11_full_35	\N	1	11	5	ApX1K+pQAA==
11_full_36	\N	1	11	5	I6lKOKUriA==
11_full_37	\N	1	11	5	BjYjEYjYwA==
11_full_38	\N	1	11	6	IUUUIYliilZA
11_full_39	\N	1	11	1	4AA=
11_full_40	\N	1	11	3	L0kkzIA=
11_full_41	\N	1	11	3	mZJJegA=
11_full_42	\N	1	11	5	AAlXEdUgAA==
11_full_43	\N	1	11	5	AAhCfIQgAA==
11_full_44	\N	1	11	3	AAAAWgA=
11_full_45	\N	1	11	5	AAAAfAAAAA==
11_full_46	\N	1	11	2	AAAo
11_full_47	\N	1	11	5	CEYjEYjEIA==
11_full_48	\N	1	11	5	duMYxjGO3A==
11_full_49	\N	1	11	3	WSSSS4A=
11_full_50	\N	1	11	6	ezhBDGcwgg/A
11_full_51	\N	1	11	6	ezBBDODBBzeA
11_full_52	\N	1	11	6	GOayii/CCCCA
11_full_53	\N	1	11	6	/ggg+DBBBzeA
11_full_54	\N	1	11	6	ezggg+jhhzeA
11_full_55	\N	1	11	5	+EIxGIxCEA==
11_full_56	\N	1	11	6	ezhhzezhhzeA
11_full_57	\N	1	11	6	ezhhxfBBBzeA
11_full_58	\N	1	11	2	AooA
11_full_59	\N	1	11	3	ACQWgAA=
11_full_60	\N	1	11	5	AEZmYYYYQA==
11_full_61	\N	1	11	5	AAAPg+AAAA==
11_full_62	\N	1	11	5	BDDDDMzEAA==
11_full_63	\N	1	11	6	ezhBDGMIIAIA
11_full_64	\N	1	11	6	ezhlrppngweA
11_full_65	\N	1	11	6	Mezhh/hhhhhA
11_full_66	\N	1	11	6	+jhhj+jhhj+A
11_full_67	\N	1	11	6	ezgggggggzeA
11_full_68	\N	1	11	6	+jhhhhhhhj+A
11_full_69	\N	1	11	5	/CEIchCEPg==
11_full_70	\N	1	11	5	/CEIchCEIA==
11_full_71	\N	1	11	6	ezgggnhhhzeA
11_full_72	\N	1	11	5	jGMY/jGMYg==
11_full_73	\N	1	11	3	6SSSS4A=
11_full_74	\N	1	11	5	CEIQhCGO3A==
11_full_75	\N	1	11	6	hjms44smijhA
11_full_76	\N	1	11	5	hCEIQhCEPg==
11_full_77	\N	1	11	7	g4+92TJgwYMGCA==
11_full_78	\N	1	11	6	hxx5ptlnjjhA
11_full_79	\N	1	11	6	ezhhhhhhhzeA
11_full_80	\N	1	11	6	+jhhj+gggggA
11_full_81	\N	1	11	6	ezhhhhhhlydA
11_full_82	\N	1	11	6	+jhhj+kmijhA
11_full_83	\N	1	11	6	ezggweDBBzeA
11_full_84	\N	1	11	5	+QhCEIQhCA==
11_full_85	\N	1	11	6	hhhhhhhhhzeA
11_full_86	\N	1	11	6	hhhhzSSSeMMA
11_full_87	\N	1	11	7	gwYMGDJk3avdEA==
11_full_88	\N	1	11	6	hhzSeMeSzhhA
11_full_89	\N	1	11	5	jGO1OIQhCA==
11_full_90	\N	1	11	6	/BDGEMIYwg/A
11_full_91	\N	1	11	3	8kkkk4A=
11_full_92	\N	1	11	5	hDCGEMIYQg==
11_full_93	\N	1	11	3	5JJJJ4A=
11_full_94	\N	1	11	5	I7cQAAAAAA==
11_full_95	\N	1	11	5	AAAAAAAAPg==
12_full_32	\N	1	12	2	AAAA
12_full_33	\N	1	12	2	qqqC
12_full_34	\N	1	12	3	toAAAAA=
12_full_35	\N	1	12	5	ABSvqV9SgAA=
12_full_36	\N	1	12	5	I6lKOKUriAA=
12_full_37	\N	1	12	6	AwzCGMMYQzDA
12_full_38	\N	1	12	6	IcUUcId1ni3d
12_full_39	\N	1	12	1	4AA=
12_full_40	\N	1	12	3	LWkkyZA=
12_full_41	\N	1	12	3	mTJJa0A=
12_full_42	\N	1	12	5	AQlXEdUhAAA=
12_full_43	\N	1	12	5	AQhCfIQhAAA=
12_full_44	\N	1	12	3	AAAAC0A=
12_full_45	\N	1	12	5	AAAAfAAAAAA=
12_full_46	\N	1	12	3	AAAAA2A=
12_full_47	\N	1	12	5	CEYjEIxGIQA=
12_full_48	\N	1	12	5	duMYxjGMduA=
12_full_49	\N	1	12	3	WSSSSXA=
12_full_50	\N	1	12	6	ezhBDGMYwgg/
12_full_51	\N	1	12	6	ezBBDODBBBze
12_full_52	\N	1	12	6	GOayii/CCCCC
12_full_53	\N	1	12	6	/gggg+DBBBze
12_full_54	\N	1	12	6	ezggg+jhhhze
12_full_55	\N	1	12	5	+EIxCMQjEIA=
12_full_56	\N	1	12	6	ezhhzezhhhze
12_full_57	\N	1	12	6	ezhhhxfBBBze
12_full_58	\N	1	12	2	AoKA
12_full_59	\N	1	12	3	ACQC0AA=
12_full_60	\N	1	12	5	AEZmYYYYQAA=
12_full_61	\N	1	12	5	AAAPgB8AAAA=
12_full_62	\N	1	12	5	BDDDDMzEAAA=
12_full_63	\N	1	12	6	ezhBBDGMIIAI
12_full_64	\N	1	12	7	fY4M2nRo2Z8DA8A=
12_full_65	\N	1	12	6	Mezhhh/hhhhh
12_full_66	\N	1	12	6	+jhhj+jhhhj+
12_full_67	\N	1	12	6	ezggggggggze
12_full_68	\N	1	12	6	+jhhhhhhhhj+
12_full_69	\N	1	12	6	/gggg8ggggg/
12_full_70	\N	1	12	6	/gggg8gggggg
12_full_71	\N	1	12	6	ezggggnhhhze
12_full_72	\N	1	12	5	jGMY/jGMYxA=
12_full_73	\N	1	12	3	6SSSSXA=
12_full_74	\N	1	12	5	CEIQhCEMduA=
12_full_75	\N	1	12	6	hjims44smijh
12_full_76	\N	1	12	5	hCEIQhCEIfA=
12_full_77	\N	1	12	9	gOD49tnMRiMBgMBgMBA=
12_full_78	\N	1	12	7	w4eNGzJmxY8OHBA=
12_full_79	\N	1	12	6	ezhhhhhhhhze
12_full_80	\N	1	12	6	+jhhhj+ggggg
12_full_81	\N	1	12	6	ezhhhhhhhlyd
12_full_82	\N	1	12	6	+jhhhj+kmijh
12_full_83	\N	1	12	6	ezggweDBBBze
12_full_84	\N	1	12	5	+QhCEIQhCEA=
12_full_85	\N	1	12	6	hhhhhhhhhhze
12_full_86	\N	1	12	6	hhhhhzSSSeMM
12_full_87	\N	1	12	9	gMBgMBgMByaSXSqdxEA=
12_full_88	\N	1	12	6	hhzSeMMeSzhh
12_full_89	\N	1	12	7	gwcaJscECBAgQIA=
12_full_90	\N	1	12	6	/BDCGMIYQwg/
12_full_91	\N	1	12	3	8kkkknA=
12_full_92	\N	1	12	5	hDCGEIYQwhA=
12_full_93	\N	1	12	3	5JJJJPA=
12_full_94	\N	1	12	5	I7cQAAAAAAA=
12_full_95	\N	1	12	5	AAAAAAAAAfA=
12_full_bold_32	\N	1	12	1	AAA=
12_full_bold_33	\N	1	12	3	2222w2A=
12_full_bold_34	\N	1	12	5	3tIAAAAAAAA=
12_full_bold_35	\N	1	12	8	AABm//9mZv//ZgAA
12_full_bold_36	\N	1	12	8	GH7/2Nj+fxsb/34Y
12_full_bold_37	\N	1	12	7	AYMYccMMOOGMGAA=
12_full_bold_38	\N	1	12	7	cfNmxx42782b+9A=
12_full_bold_39	\N	1	12	2	+AAA
12_full_bold_40	\N	1	12	4	NszMzMxj
12_full_bold_41	\N	1	12	4	xjMzMzNs
12_full_bold_42	\N	1	12	8	ABjbfjwYPH7bGAAA
12_full_bold_43	\N	1	12	6	AAMMM//MMMAA
12_full_bold_44	\N	1	12	4	AAAAAAZs
12_full_bold_45	\N	1	12	5	AAAAf+AAAAA=
12_full_bold_46	\N	1	12	3	AAAAA2A=
12_full_bold_47	\N	1	12	7	BgwwYYMMGGDDBgA=
12_full_bold_48	\N	1	12	7	ff8ePHjx48eP++A=
12_full_bold_49	\N	1	12	4	JuZmZmb/
12_full_bold_50	\N	1	12	7	ff4YMGOOMMGD//A=
12_full_bold_51	\N	1	12	7	ff4YMGOHAwcP++A=
12_full_bold_52	\N	1	12	7	HHn3bNm//wwYMGA=
12_full_bold_53	\N	1	12	7	//8GDB+fgwcP++A=
12_full_bold_54	\N	1	12	7	ff8ODB+/48eP++A=
12_full_bold_55	\N	1	12	6	//DDGGGMMMYY
12_full_bold_56	\N	1	12	7	ff8ePG+fY8eP++A=
12_full_bold_57	\N	1	12	7	ff8ePH/fgwcP++A=
12_full_bold_58	\N	1	12	3	AGwGwAA=
12_full_bold_59	\N	1	12	4	AAZgBugA
12_full_bold_60	\N	1	12	6	ADGMYwwYMGDA
12_full_bold_61	\N	1	12	5	AAH/gB/4AAA=
12_full_bold_62	\N	1	12	6	AwYMGDDGMYwA
12_full_bold_63	\N	1	12	6	e/jDDHOMMAMM
12_full_bold_64	\N	1	12	8	fv/Dz9/T09/OwP58
12_full_bold_65	\N	1	12	8	GDx+58PD///Dw8PD
12_full_bold_66	\N	1	12	8	/P7Hw8b8/MbDx/78
12_full_bold_67	\N	1	12	7	PP+eDBgwYMHN+eA=
12_full_bold_68	\N	1	12	7	/f8ePHjx48eP/+A=
12_full_bold_69	\N	1	12	7	//8GDB8+YMGD//A=
12_full_bold_70	\N	1	12	7	//8GDB8+YMGDBgA=
12_full_bold_71	\N	1	12	7	ff8eDBnz48eP++A=
12_full_bold_72	\N	1	12	6	zzzzz//zzzzz
12_full_bold_73	\N	1	12	4	/2ZmZmb/
12_full_bold_74	\N	1	12	6	DDDDDDDDDz/e
12_full_bold_75	\N	1	12	7	x5s2zZ48bNmbNjA=
12_full_bold_76	\N	1	12	6	wwwwwwwwww//
12_full_bold_77	\N	1	12	10	wPh/P//e8zwPA8DwPA8D
12_full_bold_78	\N	1	12	8	w+Pj8/Pb28/Px8fD
12_full_bold_79	\N	1	12	7	ff8ePHjx48eP++A=
12_full_bold_80	\N	1	12	7	/f8ePH//YMGDBgA=
12_full_bold_81	\N	1	12	8	fP7GxsbGxsbezv57
12_full_bold_82	\N	1	12	7	/f8ePH//bM2bHjA=
12_full_bold_83	\N	1	12	7	ff8eDB+fgweP++A=
12_full_bold_84	\N	1	12	6	//MMMMMMMMMM
12_full_bold_85	\N	1	12	7	x48ePHjx48eP++A=
12_full_bold_86	\N	1	12	7	x48ePHjbNmxw4IA=
12_full_bold_87	\N	1	12	10	wPA8DwPA8DzPt3+f4zDM
12_full_bold_88	\N	1	12	7	x48bZscONm2PHjA=
12_full_bold_89	\N	1	12	8	w8PDZmY8PBgYGBgY
12_full_bold_90	\N	1	12	7	//wYMMMMMMGD//A=
12_full_bold_91	\N	1	12	4	/8zMzMz/
12_full_bold_92	\N	1	12	7	wYGDAwYGDAwYGDA=
12_full_bold_93	\N	1	12	4	/zMzMzP/
12_full_bold_94	\N	1	12	6	MezhAAAAAAAA
12_full_bold_95	\N	1	12	5	AAAAAAAAP/A=
13_full_32	\N	1	13	1	AAA=
13_full_33	\N	1	13	1	/8g=
13_full_34	\N	1	13	3	toAAAAA=
13_full_35	\N	1	13	6	AASS/SS/SSAAAA==
13_full_36	\N	1	13	7	EPtciRofCxInW+EA
13_full_37	\N	1	13	7	AYMIMMMEGGGCGDAA
13_full_38	\N	1	13	7	MPEiR4YcbIseFneg
13_full_39	\N	1	13	1	4AA=
13_full_40	\N	1	13	4	E2yIiIxjEA==
13_full_41	\N	1	13	4	jGMRERNsgA==
13_full_42	\N	1	13	7	AABGt8cEHH2sQAAA
13_full_43	\N	1	13	5	AABCE+QhAAAA
13_full_44	\N	1	13	3	AAAAAWg=
13_full_45	\N	1	13	5	AAAAA+AAAAAA
13_full_46	\N	1	13	3	AAAAAGw=
13_full_47	\N	1	13	6	BBDCGEMIYQwggA==
13_full_48	\N	1	13	6	ezhhhhhhhhhzeA==
13_full_49	\N	1	13	3	WSSSSS4=
13_full_50	\N	1	13	7	fY4IEGGGGGGCBA/g
13_full_51	\N	1	13	7	fYwIECDHAwIEDjfA
13_full_52	\N	1	13	7	DDjTLFC/ggQIECBA
13_full_53	\N	1	13	7	/wIECB+BgQIEDjfA
13_full_54	\N	1	13	7	fY4MCBA/Q4MGDjfA
13_full_55	\N	1	13	6	/BBBDCGEMIYQQA==
13_full_56	\N	1	13	7	fY4MGDjfY4MGDjfA
13_full_57	\N	1	13	7	fY4MGDhfgQIGDjfA
13_full_58	\N	1	13	2	AKKAAA==
13_full_59	\N	1	13	3	AASC0AA=
13_full_60	\N	1	13	6	ABDGMYwYMGDBAA==
13_full_61	\N	1	13	5	AAAAfB8AAAAA
13_full_62	\N	1	13	6	AgwYMGDGMYwgAA==
13_full_63	\N	1	13	7	fY4IECDDDBAgAAEA
13_full_64	\N	1	13	7	fY4M23Ro0bM+BgeA
13_full_65	\N	1	13	7	EHG2ODB/wYMGDBgg
13_full_66	\N	1	13	7	/Q4MGDD/Q4MGDD/A
13_full_67	\N	1	13	7	fY4MCBAgQIECDjfA
13_full_68	\N	1	13	7	+RocGDBgwYMGHG+A
13_full_69	\N	1	13	7	/wIECBA+QIECBA/g
13_full_70	\N	1	13	7	/wIECBA+QIECBAgA
13_full_71	\N	1	13	7	fY4MCBAjwYMGDjfA
13_full_72	\N	1	13	6	hhhhhh/hhhhhhA==
13_full_73	\N	1	13	3	6SSSSS4=
13_full_74	\N	1	13	6	BBBBBBBBBBhzeA==
13_full_75	\N	1	13	7	gw40yxwwcLEyNDgg
13_full_76	\N	1	13	6	gggggggggggg/A==
13_full_77	\N	1	13	9	gOD49tnMRiMBgMBgMBgI
13_full_78	\N	1	13	8	wcHhobGRmYmNhYeDgw==
13_full_79	\N	1	13	7	fY4MGDBgwYMGDjfA
13_full_80	\N	1	13	7	/Q4MGDD/QIECBAgA
13_full_81	\N	1	13	7	fY4MGDBgwYMmLieg
13_full_82	\N	1	13	7	/Q4MGDD/TI0KHBgg
13_full_83	\N	1	13	7	fY4MCBgfAwIGDjfA
13_full_84	\N	1	13	7	/iBAgQIECBAgQIEA
13_full_85	\N	1	13	7	gwYMGDBgwYMGDjfA
13_full_86	\N	1	13	7	gwYMGDBxokTYocEA
13_full_87	\N	1	13	9	gMBgMBgMBgOTSS6VTuIg
13_full_88	\N	1	13	7	gwcaJscEHGyLHBgg
13_full_89	\N	1	13	7	gwcaJsUOCBAgQIEA
13_full_90	\N	1	13	8	/wEBAwYMGDBgwICA/w==
13_full_91	\N	1	13	4	+IiIiIiI8A==
13_full_92	\N	1	13	6	ggwQYIMEGCDBBA==
13_full_93	\N	1	13	4	8RERERER8A==
13_full_94	\N	1	13	6	MezhAAAAAAAAAA==
13_full_95	\N	1	13	5	AAAAAAAAAA+A
14_full_32	\N	1	14	1	AAA=
14_full_33	\N	1	14	3	22222A2A
14_full_34	\N	1	14	5	3tIAAAAAAAAA
14_full_35	\N	1	14	8	AABmZv//Zmb//2ZmAAA=
14_full_36	\N	1	14	8	GH7/2djY/n8bG5v/fhg=
14_full_37	\N	1	14	8	AOCh4wcOHDhw4MeFBwA=
14_full_38	\N	1	14	8	GDxmZmY8OHjNx8bG/3s=
14_full_39	\N	1	14	2	+AAAAA==
14_full_40	\N	1	14	4	E2zMzMzGMQ==
14_full_41	\N	1	14	4	jGMzMzM2yA==
14_full_42	\N	1	14	8	AAAYmdt+PDx+25kYAAA=
14_full_43	\N	1	14	6	AAAMMM//MMMAAAA=
14_full_44	\N	1	14	4	AAAAAAAG6A==
14_full_45	\N	1	14	5	AAAAA/8AAAAA
14_full_46	\N	1	14	3	AAAAAA2A
14_full_47	\N	1	14	8	AwMHBg4MHDgwcGDgwMA=
14_full_48	\N	1	14	7	OPu+PHjx48ePH3fHAA==
14_full_49	\N	1	14	6	Mc8MMMMMMMMM//A=
14_full_50	\N	1	14	8	fP7HAwMHDhw4cODA//8=
14_full_51	\N	1	14	8	fP7HAwMHHh4HAwPH/nw=
14_full_52	\N	1	14	8	Bg4eNmbGxv//BgYGBgY=
14_full_53	\N	1	14	8	///AwMD8fgcDAwPH/nw=
14_full_54	\N	1	14	8	Pn/jwMD8/sfDw8Pnfjw=
14_full_55	\N	1	14	7	//wYMGHDDhhww4YMAA==
14_full_56	\N	1	14	8	PH7nw8Pnfn7nw8Pnfjw=
14_full_57	\N	1	14	8	PH7nw8Pjfz8DAwPH/nw=
14_full_58	\N	1	14	3	AA2A2AAA
14_full_59	\N	1	14	4	AABmAG6AAA==
14_full_60	\N	1	14	6	ABDGMYwwYMGDBAA=
14_full_61	\N	1	14	6	AAAA//AA//AAAAA=
14_full_62	\N	1	14	6	AgwYMGDDGMYwgAA=
14_full_63	\N	1	14	8	fP7HAwMHDhwYGAAAGBg=
14_full_64	\N	1	14	9	Pj+4+Dx+fyeTz+OwHAfx8A==
14_full_65	\N	1	14	8	GDx+58PDw///w8PDw8M=
14_full_66	\N	1	14	8	/P7Hw8PH/v7Hw8PH/vw=
14_full_67	\N	1	14	8	PH7nw8DAwMDAwMPnfjw=
14_full_68	\N	1	14	8	/P7Hw8PDw8PDw8PH/vw=
14_full_69	\N	1	14	8	///AwMDA+PjAwMDA//8=
14_full_70	\N	1	14	8	///AwMDA+PjAwMDAwMA=
14_full_71	\N	1	14	8	Pn/jwMDAwMfHw8Pnfjw=
14_full_72	\N	1	14	7	x48ePHj//8ePHjx4wA==
14_full_73	\N	1	14	4	/2ZmZmZm/w==
14_full_74	\N	1	14	7	BgwYMGDBgwYPH3fHAA==
14_full_75	\N	1	14	8	w8PHztz48PD43M7Hw8M=
14_full_76	\N	1	14	7	wYMGDBgwYMGDBg//wA==
14_full_77	\N	1	14	11	wHwfx/3995zxHgPAeA8B4DwHgMA=
14_full_78	\N	1	14	9	wfD4fj8ez2ebzePx+Hw+DA==
14_full_79	\N	1	14	8	PH7nw8PDw8PDw8Pnfjw=
14_full_80	\N	1	14	8	/P7Hw8PH/vzAwMDAwMA=
14_full_81	\N	1	14	8	PH7nw8PDw8PD29/ufzs=
14_full_82	\N	1	14	8	/P7Hw8PH/vzMzMbGw8M=
14_full_83	\N	1	14	8	PH7nw8DgfD4HA8Pnfjw=
14_full_84	\N	1	14	8	//8YGBgYGBgYGBgYGBg=
14_full_85	\N	1	14	8	w8PDw8PDw8PDw8Pnfjw=
14_full_86	\N	1	14	8	w8PDw8PDw8NmZjw8GBg=
14_full_87	\N	1	14	11	wHgPAeA8B4DwHiPu7dn/PeMYYwA=
14_full_88	\N	1	14	8	w8PDZn48GBg8fmbDw8M=
14_full_89	\N	1	14	8	w8PDZmY8PBgYGBgYGBg=
14_full_90	\N	1	14	8	//8DAwcOHDhw4MDA//8=
14_full_91	\N	1	14	4	/8zMzMzM/w==
14_full_92	\N	1	14	8	wMDgYHAwOBwMDgYHAwM=
14_full_93	\N	1	14	4	/zMzMzMz/w==
14_full_94	\N	1	14	6	MezhAAAAAAAAAAA=
14_full_95	\N	1	14	6	AAAAAAAAAAAA//A=
16_full_32	\N	1	16	1	AAA=
16_full_33	\N	1	16	3	222222A2
16_full_34	\N	1	16	6	zzRAAAAAAAAAAAAA
16_full_35	\N	1	16	8	AAAAZmb//2Zm//9mZgAAAA==
16_full_36	\N	1	16	8	GBh+/9nY2P5/Gxub/34YGA==
16_full_37	\N	1	16	8	AOCh4wcGDBw4MGDgx4UHAA==
16_full_38	\N	1	16	8	OHzuxu58OHj8z8/Gxu9/OQ==
16_full_39	\N	1	16	2	+AAAAA==
16_full_40	\N	1	16	4	N+zMzMzMznM=
16_full_41	\N	1	16	4	znMzMzMzN+w=
16_full_42	\N	1	16	8	AAAY2/9+PBg8fv/bGAAAAA==
16_full_43	\N	1	16	6	AAAAMMM//MMMAAAA
16_full_44	\N	1	16	4	AAAAAAAABuw=
16_full_45	\N	1	16	6	AAAAAAA//AAAAAAA
16_full_46	\N	1	16	3	AAAAAAA2
16_full_47	\N	1	16	8	AAMDBgYMDBgYMDBgYMDAAA==
16_full_48	\N	1	16	8	PH7nw8PDw8PDw8PDw+d+PA==
16_full_49	\N	1	16	6	Mc8MMMMMMMMMMM//
16_full_50	\N	1	16	8	PH7nwwMDBw4cOHDgwMD//w==
16_full_51	\N	1	16	8	fP7HAwMDBx4eBwMDA8f+fA==
16_full_52	\N	1	16	8	Bg4ePnbmxsb//wYGBgYGBg==
16_full_53	\N	1	16	8	///AwMDA/H4HAwMDA8f+fA==
16_full_54	\N	1	16	8	Pn/jwMDA/P7Hw8PDw+d+PA==
16_full_55	\N	1	16	8	//8DAwMHBg4MHBg4MHBgYA==
16_full_56	\N	1	16	8	PH7nw8PD535+58PDw+d+PA==
16_full_57	\N	1	16	8	PH7nw8PD438/AwMDA8f+fA==
16_full_58	\N	1	16	3	AAGwGwAA
16_full_59	\N	1	16	4	AAAGYAboAAA=
16_full_60	\N	1	16	7	AAQYYYYYYMDAwMDAwIA=
16_full_61	\N	1	16	6	AAAAA//AA//AAAAA
16_full_62	\N	1	16	7	AQMDAwMDAwYYYYYYIAA=
16_full_63	\N	1	16	8	fP7HAwMDBw4cGBgYAAAYGA==
16_full_64	\N	1	16	10	Px/uHwPH8/zPM8zzPP8ewDgH+Pw=
16_full_65	\N	1	16	8	GDx+58PDw8P//8PDw8PDww==
16_full_66	\N	1	16	8	/P7Hw8PDx/7+x8PDw8f+/A==
16_full_67	\N	1	16	8	PH7nw8DAwMDAwMDAw+d+PA==
16_full_68	\N	1	16	8	/P7Hw8PDw8PDw8PDw8f+/A==
16_full_69	\N	1	16	8	///AwMDAwPj4wMDAwMD//w==
16_full_70	\N	1	16	8	///AwMDAwPj4wMDAwMDAwA==
16_full_71	\N	1	16	8	PH7nw8DAwMDHx8PDw+d+PA==
16_full_72	\N	1	16	8	w8PDw8PDw///w8PDw8PDww==
16_full_73	\N	1	16	4	/2ZmZmZmZv8=
16_full_74	\N	1	16	7	BgwYMGDBgwYMGDx93xw=
16_full_75	\N	1	16	8	w8PHxs7c+PD43MzOxsfDww==
16_full_76	\N	1	16	7	wYMGDBgwYMGDBgwYP/8=
16_full_77	\N	1	16	12	wD4H8P+f37zzxjxjwDwDwDwDwDwDwDwD
16_full_78	\N	1	16	9	weD4fD8fj2ezzebx+Pw+HweD
16_full_79	\N	1	16	8	PH7nw8PDw8PDw8PDw+d+PA==
16_full_80	\N	1	16	8	/P7Hw8PDx/78wMDAwMDAwA==
16_full_81	\N	1	16	8	PH7nw8PDw8PDw8Pb3+5/Ow==
16_full_82	\N	1	16	8	/P7Hw8PDx/78zMzGxsPDww==
16_full_83	\N	1	16	8	PH7nw8DA4Hw+BwMDw+d+PA==
16_full_84	\N	1	16	8	//8YGBgYGBgYGBgYGBgYGA==
16_full_85	\N	1	16	8	w8PDw8PDw8PDw8PDw+d+PA==
16_full_86	\N	1	16	8	w8PDw8PDw8PDZmZmPDwYGA==
16_full_87	\N	1	16	12	wDwDwDwDwDwDwDwDxjxjzz73f+eeeeMM
16_full_88	\N	1	16	8	w8PDZmY8PBgYPDxmZsPDww==
16_full_89	\N	1	16	8	w8PDZmZmPDw8GBgYGBgYGA==
16_full_90	\N	1	16	8	//8DBgYMDBgYMDBgYMD//w==
16_full_91	\N	1	16	4	/8zMzMzMzP8=
16_full_92	\N	1	16	8	AMDAYGAwMBgYDAwGBgMDAA==
16_full_93	\N	1	16	4	/zMzMzMzM/8=
16_full_94	\N	1	16	6	MezhAAAAAAAAAAAA
16_full_95	\N	1	16	6	AAAAAAAAAAAAAA//
18_full_32	\N	1	18	1	AAAA
18_full_33	\N	1	18	3	2222222A2A==
18_full_34	\N	1	18	6	zzRAAAAAAAAAAAAAAAA=
18_full_35	\N	1	18	8	AAAAZmb//2ZmZv//ZmYAAAAA
18_full_36	\N	1	18	8	GBh+/9vY2Pj+fx8bG9v/fhgY
18_full_37	\N	1	18	8	AADgoeMHBgwcODBg4MeFBwAA
18_full_38	\N	1	18	9	OD47mMzj4eBgeH7z+exmMx3ffZxA
18_full_39	\N	1	18	2	+AAAAAA=
18_full_40	\N	1	18	4	N27MzMzMzOZz
18_full_41	\N	1	18	4	zmczMzMzM3bs
18_full_42	\N	1	18	8	AAAYGJnb/348PH7/25kYGAAA
18_full_43	\N	1	18	6	AAAAAMMM//MMMAAAAAA=
18_full_44	\N	1	18	4	AAAAAAAAAAbs
18_full_45	\N	1	18	6	AAAAAAAA//AAAAAAAAA=
18_full_46	\N	1	18	3	AAAAAAAA2A==
18_full_47	\N	1	18	8	AwMHBgYODBwYGDgwcGBg4MDA
18_full_48	\N	1	18	8	PH7nw8PDw8PDw8PDw8PD5348
18_full_49	\N	1	18	6	Mc88MMMMMMMMMMMM//A=
18_full_50	\N	1	18	9	Pj+4+DAYDAYHBwcHBwcHAwGA///A
18_full_51	\N	1	18	9	fn+w4DAYDAYHDweA4DAYDAeH/z8A
18_full_52	\N	1	18	9	BgcHh8dnMxmMxn//4MBgMBgMBgMA
18_full_53	\N	1	18	9	///wGAwGAwH8fwHAYDAYDAeH/z8A
18_full_54	\N	1	18	9	Pz/4eAwGAwH8/2HweDweDwfHfx8A
18_full_55	\N	1	18	8	//8DBwYGDgwMHBgYODAwcGBg
18_full_56	\N	1	18	9	Pj+4+DweD47+Pj+4+DweDwfHfx8A
18_full_57	\N	1	18	9	Pj+4+DweDwfDf5/AYDAYDAeH/z8A
18_full_58	\N	1	18	3	AAA2A2AAAA==
18_full_59	\N	1	18	4	AAAAZgBugAAA
18_full_60	\N	1	18	8	AAEDBw4cOHDg4HA4HA4HAwEA
18_full_61	\N	1	18	6	AAAAAA//AA//AAAAAAA=
18_full_62	\N	1	18	8	AIDA4HA4HA4HBw4cOHDgwIAA
18_full_63	\N	1	18	9	fn+w4DAYDAYHBwcHAwGAwAAAGAwA
18_full_64	\N	1	18	10	Px/uHwPA8fz/M8zzPM8/x7AMA4B/j8A=
18_full_65	\N	1	18	9	CA4Pju4+DweDwf//+DweDweDweDA
18_full_66	\N	1	18	9	/n+w+DweDw/+/2HweDweDweH/38A
18_full_67	\N	1	18	9	Pj+4+DwGAwGAwGAwGAwGAwfHfx8A
18_full_68	\N	1	18	9	/n+w+DweDweDweDweDweDweH/38A
18_full_69	\N	1	18	9	///wGAwGAwH4/GAwGAwGAwGA///A
18_full_70	\N	1	18	9	///wGAwGAwH4/GAwGAwGAwGAwGAA
18_full_71	\N	1	18	9	Pj+4+DwGAwGAwGHw+DweDwfHfx8A
18_full_72	\N	1	18	8	w8PDw8PDw8P//8PDw8PDw8PD
18_full_73	\N	1	18	4	/2ZmZmZmZmb/
18_full_74	\N	1	18	7	BgwYMGDBgwYMGDBg8fd8cA==
18_full_75	\N	1	18	9	weDw+Gx2c3Hw8Hw3GcxmOw2HweDA
18_full_76	\N	1	18	8	wMDAwMDAwMDAwMDAwMDAwP//
18_full_77	\N	1	18	12	wD4H8P8P+f37zzxjxjwDwDwDwDwDwDwDwDwD
18_full_78	\N	1	18	10	wPA+D4Pw/D2PY8zzPG8bw/D8HwfA8DA=
18_full_79	\N	1	18	9	Pj+4+DweDweDweDweDweDwfHfx8A
18_full_80	\N	1	18	9	/n+w+DweDw/+/mAwGAwGAwGAwGAA
18_full_81	\N	1	18	9	Pj+4+DweDweDweDweDwebz/Of57A
18_full_82	\N	1	18	9	/n+w+DweDw/+/mYzGcxmOw2HweDA
18_full_83	\N	1	18	9	Pz/4eAwGAwHAfh+A4DAYDAeH/z8A
18_full_84	\N	1	18	8	//8YGBgYGBgYGBgYGBgYGBgY
18_full_85	\N	1	18	9	weDweDweDweDweDweDweDwfHfx8A
18_full_86	\N	1	18	9	weDweDweDweDwfHYzuNh8HA4CAQA
18_full_87	\N	1	18	12	wDwDwDwDwDwDwDwDwDxjxjzz73//f+eeMMMM
18_full_88	\N	1	18	9	weDwfHYzGdx8HA4PjuYzG4+DweDA
18_full_89	\N	1	18	8	w8PD52Zmfjw8PBgYGBgYGBgY
18_full_90	\N	1	18	9	///AYDA4GBwcHA4ODgYHAwGA///A
18_full_91	\N	1	18	5	//GMYxjGMYxjGP/A
18_full_92	\N	1	18	8	wMDgYGBwMDgYGBwMDgYGBwMD
18_full_93	\N	1	18	5	/8YxjGMYxjGMY//A
18_full_94	\N	1	18	7	EHH3fHBAAAAAAAAAAAAAAA==
18_full_95	\N	1	18	7	AAAAAAAAAAAAAAAAAAD//A==
20_full_48	\N	1	20	9	Pj+4+DweDweDweDweDweDweDwfHfx8A=
20_full_49	\N	1	20	6	Mc88MMMMMMMMMMMMMM//
20_full_50	\N	1	20	10	Px/uHwMAwDAMBwOBwOBwOBwOAwDAMA///w==
20_full_51	\N	1	20	10	Px/uHwMAwDAMAwHB4HgHAMAwDAPA+Hf4/A==
20_full_52	\N	1	20	10	AwHA8Hw7HM4zDMMwz///AwDAMAwDAMAwDA==
20_full_53	\N	1	20	10	///8AwDAMAwDAP8f4BwDAMAwDAMA8H/5/A==
20_full_54	\N	1	20	10	P5/+DwDAMAwDAP8/7B8DwPA8DwPA+Hf4/A==
20_full_55	\N	1	20	9	///AYDAYHAwOBgcDA4GBwMBgcDAYDAA=
20_full_56	\N	1	20	10	Px/uHwPA8DwPh3+Px/uHwPA8DwPA+Hf4/A==
20_full_57	\N	1	20	10	Px/uHwPA8DwPA+Df8/wDAMAwDAMA8H/5/A==
24_full_48	\N	1	24	12	H4P8cO4HwDwDwDwDwDwDwDwDwDwDwDwDwDwDwDwD4HcOP8H4
24_full_49	\N	1	24	6	EMc88MMMMMMMMMMMMMMMMM//
24_full_50	\N	1	24	12	P4f84OwHADADADADAHAOAcA4BwDgHAOAcA4AwAwAwAwA////
24_full_51	\N	1	24	12	P4f84OwHADADADADADAHAOA8A8AOAHADADADADADwH4Of8P4
24_full_52	\N	1	24	12	AMAcA8B8DsHMOMcM4MwMwMwM////AMAMAMAMAMAMAMAMAMAM
24_full_53	\N	1	24	12	////wAwAwAwAwAwAwA4A/8f+AHADADADADADADADAH4O/8P4
24_full_54	\N	1	24	12	H+P/cD4AwAwAwAwAwAwA/4/84OwHwDwDwDwDwDwD4HcOP8H4
24_full_55	\N	1	24	11	///8AYAwBgDAGAcAwDgGAcAwDgGAcAwDgGAcAwDgGAMA
24_full_56	\N	1	24	12	H4P8cO4HwDwDwDwD4HcOP8P8cO4HwDwDwDwDwDwD4HcOP8H4
24_full_57	\N	1	24	12	H4P8cO4HwDwDwDwDwDwD4Hf/P/ADADADADADADADAHwO/8f4
_09_full_12_32	\N	1	12	1	AAA=
_09_full_12_33	\N	1	12	2	qqiA
_09_full_12_34	\N	1	12	3	tAAAAAA=
_09_full_12_35	\N	1	12	5	Ur6lfUoAAAA=
_09_full_12_36	\N	1	12	5	I6lHFK4gAAA=
_09_full_12_37	\N	1	12	5	BnQiIXMAAAA=
_09_full_12_38	\N	1	12	6	IUUIYliidBAA
_09_full_12_39	\N	1	12	1	wAA=
_09_full_12_40	\N	1	12	3	KkkiIAA=
_09_full_12_41	\N	1	12	3	iJJKgAA=
_09_full_12_42	\N	1	12	5	ASriOqQAAAA=
_09_full_12_43	\N	1	12	5	AAhPkIAAAAA=
_09_full_12_44	\N	1	12	3	AAACUAA=
_09_full_12_45	\N	1	12	4	AADwAAAA
_09_full_12_46	\N	1	12	2	AACA
_09_full_12_47	\N	1	12	5	CEQiIRCAAAA=
_09_full_12_48	\N	1	12	5	dGMYxjFwAAA=
_09_full_12_49	\N	1	12	3	WSSS4AA=
_09_full_12_50	\N	1	12	5	dEIiIhD4AAA=
_09_full_12_51	\N	1	12	5	dEITBDFwAAA=
_09_full_12_52	\N	1	12	5	EZUpfEIQAAA=
_09_full_12_53	\N	1	12	5	/CEPBDFwAAA=
_09_full_12_54	\N	1	12	5	dGEPRjFwAAA=
_09_full_12_55	\N	1	12	5	+EIhEIhAAAA=
_09_full_12_56	\N	1	12	5	dGMXRjFwAAA=
_09_full_12_57	\N	1	12	5	dGMXhDFwAAA=
_09_full_12_58	\N	1	12	2	AiAA
_09_full_12_59	\N	1	12	3	ACCgAAA=
_09_full_12_60	\N	1	12	4	ASSEIQAA
_09_full_12_61	\N	1	12	4	AA8PAAAA
_09_full_12_62	\N	1	12	4	CEISSAAA
_09_full_12_63	\N	1	12	5	dEIREIAgAAA=
_09_full_12_64	\N	1	12	6	ehlrppngeAAA
_09_full_12_65	\N	1	12	5	IqMfxjGIAAA=
_09_full_12_66	\N	1	12	5	9GMfRjHwAAA=
_09_full_12_67	\N	1	12	5	dGEIQhFwAAA=
_09_full_12_68	\N	1	12	5	9GMYxjHwAAA=
_09_full_12_69	\N	1	12	5	/CEOQhD4AAA=
_09_full_12_70	\N	1	12	5	/CEOQhCAAAA=
_09_full_12_71	\N	1	12	5	dGEJxjFwAAA=
_09_full_12_72	\N	1	12	5	jGMfxjGIAAA=
_09_full_12_73	\N	1	12	3	6SSS4AA=
_09_full_12_74	\N	1	12	4	ERERGWAA
_09_full_12_75	\N	1	12	5	jGVMUlGIAAA=
_09_full_12_76	\N	1	12	4	iIiIiPAA
_09_full_12_77	\N	1	12	7	g46smTBgwYIAAAA=
_09_full_12_78	\N	1	12	6	xxpplljjhAAA
_09_full_12_79	\N	1	12	5	dGMYxjFwAAA=
_09_full_12_80	\N	1	12	5	9GMfQhCAAAA=
_09_full_12_81	\N	1	12	5	dGMYxrJoQAA=
_09_full_12_82	\N	1	12	5	9GMfUlGIAAA=
_09_full_12_83	\N	1	12	5	dGEHBDFwAAA=
_09_full_12_84	\N	1	12	5	+QhCEIQgAAA=
_09_full_12_85	\N	1	12	5	jGMYxjFwAAA=
_09_full_12_86	\N	1	12	5	jGMYqUQgAAA=
_09_full_12_87	\N	1	12	7	gwYMGTJq1UQAAAA=
_09_full_12_88	\N	1	12	5	jGKiKjGIAAA=
_09_full_12_89	\N	1	12	5	jGKiEIQgAAA=
_09_full_12_90	\N	1	12	5	+EQiIRD4AAA=
_09_full_12_91	\N	1	12	3	8kkk4AA=
_09_full_12_92	\N	1	12	5	hBCCCEEIAAA=
_09_full_12_93	\N	1	12	3	5JJJ4AA=
_09_full_12_94	\N	1	12	5	IqIAAAAAAAA=
_09_full_12_95	\N	1	12	4	AAAAAA8A
_09_full_12_96	\N	1	12	2	pAAA
_09_full_12_97	\N	1	12	5	AADgvjNoAAA=
_09_full_12_98	\N	1	12	5	hCEPRjmwAAA=
_09_full_12_99	\N	1	12	4	AAB4iHAA
_09_full_12_100	\N	1	12	5	CEIXxjNoAAA=
_09_full_12_101	\N	1	12	5	AADox9BwAAA=
_09_full_12_102	\N	1	12	4	NET0REAA
_09_full_12_103	\N	1	12	5	AAAGzjF4YuA=
_09_full_12_104	\N	1	12	5	hCELZjGIAAA=
_09_full_12_105	\N	1	12	3	AQSSQAA=
_09_full_12_106	\N	1	12	4	ABARERGW
_09_full_12_107	\N	1	12	5	hCMqYpKIAAA=
_09_full_12_108	\N	1	12	3	SSSSQAA=
_09_full_12_109	\N	1	12	7	AAAACtpkyZIAAAA=
_09_full_12_110	\N	1	12	5	AAALZjGIAAA=
_09_full_12_111	\N	1	12	5	AAAHRjFwAAA=
_09_full_12_112	\N	1	12	5	AAALZjH0IQA=
_09_full_12_113	\N	1	12	5	AAAGzjF4QhA=
_09_full_12_114	\N	1	12	4	AAC8iIAA
_09_full_12_115	\N	1	12	5	AADosFFwAAA=
_09_full_12_116	\N	1	12	4	AETkRDAA
_09_full_12_117	\N	1	12	5	AAAIxjNoAAA=
_09_full_12_118	\N	1	12	5	AAAIxiogAAA=
_09_full_12_119	\N	1	12	7	AAAACDBk1UQAAAA=
_09_full_12_120	\N	1	12	5	AAAIqIqIAAA=
_09_full_12_121	\N	1	12	5	AAAIxjNoQuA=
_09_full_12_122	\N	1	12	5	AAHxERD4AAA=
_09_full_12_123	\N	1	12	3	aSiSYAA=
_09_full_12_124	\N	1	12	1	94A=
_09_full_12_125	\N	1	12	3	ySKSwAA=
_7_full_32	\N	1	7	1	AA==
_7_full_33	\N	1	7	2	qog=
_7_full_34	\N	1	7	3	tAAA
_7_full_35	\N	1	7	5	Ur6vqUA=
_7_full_36	\N	1	7	5	I6jiuIA=
_7_full_37	\N	1	7	5	xkRETGA=
_7_full_38	\N	1	7	5	RSiKyaA=
_7_full_39	\N	1	7	1	wA==
_7_full_40	\N	1	7	3	KkiI
_7_full_41	\N	1	7	3	iJKg
_7_full_42	\N	1	7	7	EFEUFEUEAA==
_7_full_43	\N	1	7	5	AQnyEAA=
_7_full_44	\N	1	7	3	AACg
_7_full_45	\N	1	7	4	AA8AAA==
_7_full_46	\N	1	7	2	AAg=
_7_full_47	\N	1	7	5	AEREQAA=
_7_full_48	\N	1	7	4	aZmZYA==
_7_full_49	\N	1	7	3	WSS4
_7_full_50	\N	1	7	4	aRJI8A==
_7_full_51	\N	1	7	4	aRYZYA==
_7_full_52	\N	1	7	5	EZUviEA=
_7_full_53	\N	1	7	4	+IYZYA==
_7_full_54	\N	1	7	4	aY6ZYA==
_7_full_55	\N	1	7	4	8RIkQA==
_7_full_56	\N	1	7	4	aZaZYA==
_7_full_57	\N	1	7	4	aZcZYA==
_7_full_58	\N	1	7	2	CIA=
_7_full_59	\N	1	7	3	AQUA
_7_full_60	\N	1	7	4	EkhCEA==
_7_full_61	\N	1	7	4	APDwAA==
_7_full_62	\N	1	7	4	hCEkgA==
_7_full_63	\N	1	7	4	aRIgIA==
_7_full_64	\N	1	7	5	dGdZwcA=
_7_full_65	\N	1	7	4	aZ+ZkA==
_7_full_66	\N	1	7	4	6Z6Z4A==
_7_full_67	\N	1	7	4	aYiJYA==
_7_full_68	\N	1	7	4	6ZmZ4A==
_7_full_69	\N	1	7	4	+I6I8A==
_7_full_70	\N	1	7	4	+I6IgA==
_7_full_71	\N	1	7	4	aYuZcA==
_7_full_72	\N	1	7	4	mZ+ZkA==
_7_full_73	\N	1	7	3	6SS4
_7_full_74	\N	1	7	4	EREZYA==
_7_full_75	\N	1	7	4	maypkA==
_7_full_76	\N	1	7	4	iIiI8A==
_7_full_77	\N	1	7	7	g46smTBggA==
_7_full_78	\N	1	7	5	jnNZziA=
_7_full_79	\N	1	7	4	aZmZYA==
_7_full_80	\N	1	7	4	6Z6IgA==
_7_full_81	\N	1	7	5	dGMayaA=
_7_full_82	\N	1	7	4	6Z6pkA==
_7_full_83	\N	1	7	4	aYYZYA==
_7_full_84	\N	1	7	5	+QhCEIA=
_7_full_85	\N	1	7	4	mZmZYA==
_7_full_86	\N	1	7	5	jGMVKIA=
_7_full_87	\N	1	7	7	gwYMmrVRAA==
_7_full_88	\N	1	7	5	jFRFRiA=
_7_full_89	\N	1	7	5	jFRCEIA=
_7_full_90	\N	1	7	5	+EREQ+A=
_7_full_91	\N	1	7	3	8kk4
_7_full_92	\N	1	7	5	BBBBBAA=
_7_full_93	\N	1	7	3	5JJ4
_7_full_94	\N	1	7	5	IqIAAAA=
_7_full_95	\N	1	7	5	AAAAA+A=
\.

-- load iris.glyph
COPY iris.glyph (name, font, code_point, graphic) FROM stdin;
Ver-Mac 5x7_32	Ver-Mac 5x7	32	Ver-Mac 5x7_32
Ver-Mac 5x7_33	Ver-Mac 5x7	33	Ver-Mac 5x7_33
Ver-Mac 5x7_34	Ver-Mac 5x7	34	Ver-Mac 5x7_34
Ver-Mac 5x7_35	Ver-Mac 5x7	35	Ver-Mac 5x7_35
Ver-Mac 5x7_36	Ver-Mac 5x7	36	Ver-Mac 5x7_36
Ver-Mac 5x7_37	Ver-Mac 5x7	37	Ver-Mac 5x7_37
Ver-Mac 5x7_38	Ver-Mac 5x7	38	Ver-Mac 5x7_38
Ver-Mac 5x7_39	Ver-Mac 5x7	39	Ver-Mac 5x7_39
Ver-Mac 5x7_40	Ver-Mac 5x7	40	Ver-Mac 5x7_40
Ver-Mac 5x7_41	Ver-Mac 5x7	41	Ver-Mac 5x7_41
Ver-Mac 5x7_42	Ver-Mac 5x7	42	Ver-Mac 5x7_42
Ver-Mac 5x7_43	Ver-Mac 5x7	43	Ver-Mac 5x7_43
Ver-Mac 5x7_44	Ver-Mac 5x7	44	Ver-Mac 5x7_44
Ver-Mac 5x7_45	Ver-Mac 5x7	45	Ver-Mac 5x7_45
Ver-Mac 5x7_46	Ver-Mac 5x7	46	Ver-Mac 5x7_46
Ver-Mac 5x7_47	Ver-Mac 5x7	47	Ver-Mac 5x7_47
Ver-Mac 5x7_48	Ver-Mac 5x7	48	Ver-Mac 5x7_48
Ver-Mac 5x7_49	Ver-Mac 5x7	49	Ver-Mac 5x7_49
Ver-Mac 5x7_50	Ver-Mac 5x7	50	Ver-Mac 5x7_50
Ver-Mac 5x7_51	Ver-Mac 5x7	51	Ver-Mac 5x7_51
Ver-Mac 5x7_52	Ver-Mac 5x7	52	Ver-Mac 5x7_52
Ver-Mac 5x7_53	Ver-Mac 5x7	53	Ver-Mac 5x7_53
Ver-Mac 5x7_54	Ver-Mac 5x7	54	Ver-Mac 5x7_54
Ver-Mac 5x7_55	Ver-Mac 5x7	55	Ver-Mac 5x7_55
Ver-Mac 5x7_56	Ver-Mac 5x7	56	Ver-Mac 5x7_56
Ver-Mac 5x7_57	Ver-Mac 5x7	57	Ver-Mac 5x7_57
Ver-Mac 5x7_58	Ver-Mac 5x7	58	Ver-Mac 5x7_58
Ver-Mac 5x7_59	Ver-Mac 5x7	59	Ver-Mac 5x7_59
Ver-Mac 5x7_60	Ver-Mac 5x7	60	Ver-Mac 5x7_60
Ver-Mac 5x7_61	Ver-Mac 5x7	61	Ver-Mac 5x7_61
Ver-Mac 5x7_62	Ver-Mac 5x7	62	Ver-Mac 5x7_62
Ver-Mac 5x7_63	Ver-Mac 5x7	63	Ver-Mac 5x7_63
Ver-Mac 5x7_64	Ver-Mac 5x7	64	Ver-Mac 5x7_64
Ver-Mac 5x7_65	Ver-Mac 5x7	65	Ver-Mac 5x7_65
Ver-Mac 5x7_66	Ver-Mac 5x7	66	Ver-Mac 5x7_66
Ver-Mac 5x7_67	Ver-Mac 5x7	67	Ver-Mac 5x7_67
Ver-Mac 5x7_68	Ver-Mac 5x7	68	Ver-Mac 5x7_68
Ver-Mac 5x7_69	Ver-Mac 5x7	69	Ver-Mac 5x7_69
Ver-Mac 5x7_70	Ver-Mac 5x7	70	Ver-Mac 5x7_70
Ver-Mac 5x7_71	Ver-Mac 5x7	71	Ver-Mac 5x7_71
Ver-Mac 5x7_72	Ver-Mac 5x7	72	Ver-Mac 5x7_72
Ver-Mac 5x7_73	Ver-Mac 5x7	73	Ver-Mac 5x7_73
Ver-Mac 5x7_74	Ver-Mac 5x7	74	Ver-Mac 5x7_74
Ver-Mac 5x7_75	Ver-Mac 5x7	75	Ver-Mac 5x7_75
Ver-Mac 5x7_76	Ver-Mac 5x7	76	Ver-Mac 5x7_76
Ver-Mac 5x7_77	Ver-Mac 5x7	77	Ver-Mac 5x7_77
Ver-Mac 5x7_78	Ver-Mac 5x7	78	Ver-Mac 5x7_78
Ver-Mac 5x7_79	Ver-Mac 5x7	79	Ver-Mac 5x7_79
Ver-Mac 5x7_80	Ver-Mac 5x7	80	Ver-Mac 5x7_80
Ver-Mac 5x7_81	Ver-Mac 5x7	81	Ver-Mac 5x7_81
Ver-Mac 5x7_82	Ver-Mac 5x7	82	Ver-Mac 5x7_82
Ver-Mac 5x7_83	Ver-Mac 5x7	83	Ver-Mac 5x7_83
Ver-Mac 5x7_84	Ver-Mac 5x7	84	Ver-Mac 5x7_84
Ver-Mac 5x7_85	Ver-Mac 5x7	85	Ver-Mac 5x7_85
Ver-Mac 5x7_86	Ver-Mac 5x7	86	Ver-Mac 5x7_86
Ver-Mac 5x7_87	Ver-Mac 5x7	87	Ver-Mac 5x7_87
Ver-Mac 5x7_88	Ver-Mac 5x7	88	Ver-Mac 5x7_88
Ver-Mac 5x7_89	Ver-Mac 5x7	89	Ver-Mac 5x7_89
Ver-Mac 5x7_90	Ver-Mac 5x7	90	Ver-Mac 5x7_90
Ver-Mac 5x7_91	Ver-Mac 5x7	91	Ver-Mac 5x7_91
Ver-Mac 5x7_92	Ver-Mac 5x7	92	Ver-Mac 5x7_92
Ver-Mac 5x7_93	Ver-Mac 5x7	93	Ver-Mac 5x7_93
Ver-Mac 5x7_94	Ver-Mac 5x7	94	Ver-Mac 5x7_94
Ver-Mac 5x7_95	Ver-Mac 5x7	95	Ver-Mac 5x7_95
Ver-Mac 5x7_96	Ver-Mac 5x7	96	Ver-Mac 5x7_96
Ver-Mac 5x7_97	Ver-Mac 5x7	97	Ver-Mac 5x7_97
Ver-Mac 5x7_98	Ver-Mac 5x7	98	Ver-Mac 5x7_98
Ver-Mac 5x7_99	Ver-Mac 5x7	99	Ver-Mac 5x7_99
Ver-Mac 5x7_100	Ver-Mac 5x7	100	Ver-Mac 5x7_100
Ver-Mac 5x7_101	Ver-Mac 5x7	101	Ver-Mac 5x7_101
Ver-Mac 5x7_102	Ver-Mac 5x7	102	Ver-Mac 5x7_102
Ver-Mac 5x7_103	Ver-Mac 5x7	103	Ver-Mac 5x7_103
Ver-Mac 5x7_104	Ver-Mac 5x7	104	Ver-Mac 5x7_104
Ver-Mac 5x7_105	Ver-Mac 5x7	105	Ver-Mac 5x7_105
Ver-Mac 5x7_106	Ver-Mac 5x7	106	Ver-Mac 5x7_106
Ver-Mac 5x7_107	Ver-Mac 5x7	107	Ver-Mac 5x7_107
Ver-Mac 5x7_108	Ver-Mac 5x7	108	Ver-Mac 5x7_108
Ver-Mac 5x7_109	Ver-Mac 5x7	109	Ver-Mac 5x7_109
Ver-Mac 5x7_110	Ver-Mac 5x7	110	Ver-Mac 5x7_110
Ver-Mac 5x7_111	Ver-Mac 5x7	111	Ver-Mac 5x7_111
Ver-Mac 5x7_112	Ver-Mac 5x7	112	Ver-Mac 5x7_112
Ver-Mac 5x7_113	Ver-Mac 5x7	113	Ver-Mac 5x7_113
Ver-Mac 5x7_114	Ver-Mac 5x7	114	Ver-Mac 5x7_114
Ver-Mac 5x7_115	Ver-Mac 5x7	115	Ver-Mac 5x7_115
Ver-Mac 5x7_116	Ver-Mac 5x7	116	Ver-Mac 5x7_116
Ver-Mac 5x7_117	Ver-Mac 5x7	117	Ver-Mac 5x7_117
Ver-Mac 5x7_118	Ver-Mac 5x7	118	Ver-Mac 5x7_118
Ver-Mac 5x7_119	Ver-Mac 5x7	119	Ver-Mac 5x7_119
Ver-Mac 5x7_120	Ver-Mac 5x7	120	Ver-Mac 5x7_120
Ver-Mac 5x7_121	Ver-Mac 5x7	121	Ver-Mac 5x7_121
Ver-Mac 5x7_122	Ver-Mac 5x7	122	Ver-Mac 5x7_122
Ver-Mac 5x7_123	Ver-Mac 5x7	123	Ver-Mac 5x7_123
Ver-Mac 5x7_124	Ver-Mac 5x7	124	Ver-Mac 5x7_124
Ver-Mac 5x7_125	Ver-Mac 5x7	125	Ver-Mac 5x7_125
Ver-Mac 5x7_126	Ver-Mac 5x7	126	Ver-Mac 5x7_126
07_char_32	07_char	32	07_char_32
07_char_33	07_char	33	07_char_33
07_char_34	07_char	34	07_char_34
07_char_35	07_char	35	07_char_35
07_char_36	07_char	36	07_char_36
07_char_37	07_char	37	07_char_37
07_char_38	07_char	38	07_char_38
07_char_39	07_char	39	07_char_39
07_char_40	07_char	40	07_char_40
07_char_41	07_char	41	07_char_41
07_char_42	07_char	42	07_char_42
07_char_43	07_char	43	07_char_43
07_char_44	07_char	44	07_char_44
07_char_45	07_char	45	07_char_45
07_char_46	07_char	46	07_char_46
07_char_47	07_char	47	07_char_47
07_char_48	07_char	48	07_char_48
07_char_49	07_char	49	07_char_49
07_char_50	07_char	50	07_char_50
07_char_51	07_char	51	07_char_51
07_char_52	07_char	52	07_char_52
07_char_53	07_char	53	07_char_53
07_char_54	07_char	54	07_char_54
07_char_55	07_char	55	07_char_55
07_char_56	07_char	56	07_char_56
07_char_57	07_char	57	07_char_57
07_char_58	07_char	58	07_char_58
07_char_59	07_char	59	07_char_59
07_char_60	07_char	60	07_char_60
07_char_61	07_char	61	07_char_61
07_char_62	07_char	62	07_char_62
07_char_63	07_char	63	07_char_63
07_char_64	07_char	64	07_char_64
07_char_65	07_char	65	07_char_65
07_char_66	07_char	66	07_char_66
07_char_67	07_char	67	07_char_67
07_char_68	07_char	68	07_char_68
07_char_69	07_char	69	07_char_69
07_char_70	07_char	70	07_char_70
07_char_71	07_char	71	07_char_71
07_char_72	07_char	72	07_char_72
07_char_73	07_char	73	07_char_73
07_char_74	07_char	74	07_char_74
07_char_75	07_char	75	07_char_75
07_char_76	07_char	76	07_char_76
07_char_77	07_char	77	07_char_77
07_char_78	07_char	78	07_char_78
07_char_79	07_char	79	07_char_79
07_char_80	07_char	80	07_char_80
07_char_81	07_char	81	07_char_81
07_char_82	07_char	82	07_char_82
07_char_83	07_char	83	07_char_83
07_char_84	07_char	84	07_char_84
07_char_85	07_char	85	07_char_85
07_char_86	07_char	86	07_char_86
07_char_87	07_char	87	07_char_87
07_char_88	07_char	88	07_char_88
07_char_89	07_char	89	07_char_89
07_char_90	07_char	90	07_char_90
07_char_91	07_char	91	07_char_91
07_char_92	07_char	92	07_char_92
07_char_93	07_char	93	07_char_93
07_char_94	07_char	94	07_char_94
07_char_95	07_char	95	07_char_95
07_line_32	07_line	32	07_line_32
07_line_33	07_line	33	07_line_33
07_line_34	07_line	34	07_line_34
07_line_35	07_line	35	07_line_35
07_line_36	07_line	36	07_line_36
07_line_37	07_line	37	07_line_37
07_line_38	07_line	38	07_line_38
07_line_39	07_line	39	07_line_39
07_line_40	07_line	40	07_line_40
07_line_41	07_line	41	07_line_41
07_line_42	07_line	42	07_line_42
07_line_43	07_line	43	07_line_43
07_line_44	07_line	44	07_line_44
07_line_45	07_line	45	07_line_45
07_line_46	07_line	46	07_line_46
07_line_47	07_line	47	07_line_47
07_line_48	07_line	48	07_line_48
07_line_49	07_line	49	07_line_49
07_line_50	07_line	50	07_line_50
07_line_51	07_line	51	07_line_51
07_line_52	07_line	52	07_line_52
07_line_53	07_line	53	07_line_53
07_line_54	07_line	54	07_line_54
07_line_55	07_line	55	07_line_55
07_line_56	07_line	56	07_line_56
07_line_57	07_line	57	07_line_57
07_line_58	07_line	58	07_line_58
07_line_59	07_line	59	07_line_59
07_line_60	07_line	60	07_line_60
07_line_61	07_line	61	07_line_61
07_line_62	07_line	62	07_line_62
07_line_63	07_line	63	07_line_63
07_line_64	07_line	64	07_line_64
07_line_65	07_line	65	07_line_65
07_line_66	07_line	66	07_line_66
07_line_67	07_line	67	07_line_67
07_line_68	07_line	68	07_line_68
07_line_69	07_line	69	07_line_69
07_line_70	07_line	70	07_line_70
07_line_71	07_line	71	07_line_71
07_line_72	07_line	72	07_line_72
07_line_73	07_line	73	07_line_73
07_line_74	07_line	74	07_line_74
07_line_75	07_line	75	07_line_75
07_line_76	07_line	76	07_line_76
07_line_77	07_line	77	07_line_77
07_line_78	07_line	78	07_line_78
07_line_79	07_line	79	07_line_79
07_line_80	07_line	80	07_line_80
07_line_81	07_line	81	07_line_81
07_line_82	07_line	82	07_line_82
07_line_83	07_line	83	07_line_83
07_line_84	07_line	84	07_line_84
07_line_85	07_line	85	07_line_85
07_line_86	07_line	86	07_line_86
07_line_87	07_line	87	07_line_87
07_line_88	07_line	88	07_line_88
07_line_89	07_line	89	07_line_89
07_line_90	07_line	90	07_line_90
07_line_91	07_line	91	07_line_91
07_line_92	07_line	92	07_line_92
07_line_93	07_line	93	07_line_93
07_line_94	07_line	94	07_line_94
07_line_95	07_line	95	07_line_95
08_full_32	08_full	32	08_full_32
08_full_33	08_full	33	08_full_33
08_full_34	08_full	34	08_full_34
08_full_35	08_full	35	08_full_35
08_full_36	08_full	36	08_full_36
08_full_37	08_full	37	08_full_37
08_full_38	08_full	38	08_full_38
08_full_39	08_full	39	08_full_39
08_full_40	08_full	40	08_full_40
08_full_41	08_full	41	08_full_41
08_full_42	08_full	42	08_full_42
08_full_43	08_full	43	08_full_43
08_full_44	08_full	44	08_full_44
08_full_45	08_full	45	08_full_45
08_full_46	08_full	46	08_full_46
08_full_47	08_full	47	08_full_47
08_full_48	08_full	48	08_full_48
08_full_49	08_full	49	08_full_49
08_full_50	08_full	50	08_full_50
08_full_51	08_full	51	08_full_51
08_full_52	08_full	52	08_full_52
08_full_53	08_full	53	08_full_53
08_full_54	08_full	54	08_full_54
08_full_55	08_full	55	08_full_55
08_full_56	08_full	56	08_full_56
08_full_57	08_full	57	08_full_57
08_full_58	08_full	58	08_full_58
08_full_59	08_full	59	08_full_59
08_full_60	08_full	60	08_full_60
08_full_61	08_full	61	08_full_61
08_full_62	08_full	62	08_full_62
08_full_63	08_full	63	08_full_63
08_full_64	08_full	64	08_full_64
08_full_65	08_full	65	08_full_65
08_full_66	08_full	66	08_full_66
08_full_67	08_full	67	08_full_67
08_full_68	08_full	68	08_full_68
08_full_69	08_full	69	08_full_69
08_full_70	08_full	70	08_full_70
08_full_71	08_full	71	08_full_71
08_full_72	08_full	72	08_full_72
08_full_73	08_full	73	08_full_73
08_full_74	08_full	74	08_full_74
08_full_75	08_full	75	08_full_75
08_full_76	08_full	76	08_full_76
08_full_77	08_full	77	08_full_77
08_full_78	08_full	78	08_full_78
08_full_79	08_full	79	08_full_79
08_full_80	08_full	80	08_full_80
08_full_81	08_full	81	08_full_81
08_full_82	08_full	82	08_full_82
08_full_83	08_full	83	08_full_83
08_full_84	08_full	84	08_full_84
08_full_85	08_full	85	08_full_85
08_full_86	08_full	86	08_full_86
08_full_87	08_full	87	08_full_87
08_full_88	08_full	88	08_full_88
08_full_89	08_full	89	08_full_89
08_full_90	08_full	90	08_full_90
08_full_91	08_full	91	08_full_91
08_full_92	08_full	92	08_full_92
08_full_93	08_full	93	08_full_93
08_full_94	08_full	94	08_full_94
08_full_95	08_full	95	08_full_95
09_full_32	09_full	32	09_full_32
09_full_33	09_full	33	09_full_33
09_full_34	09_full	34	09_full_34
09_full_35	09_full	35	09_full_35
09_full_36	09_full	36	09_full_36
09_full_37	09_full	37	09_full_37
09_full_38	09_full	38	09_full_38
09_full_39	09_full	39	09_full_39
09_full_40	09_full	40	09_full_40
09_full_41	09_full	41	09_full_41
09_full_42	09_full	42	09_full_42
09_full_43	09_full	43	09_full_43
09_full_44	09_full	44	09_full_44
09_full_45	09_full	45	09_full_45
09_full_46	09_full	46	09_full_46
09_full_47	09_full	47	09_full_47
09_full_48	09_full	48	09_full_48
09_full_49	09_full	49	09_full_49
09_full_50	09_full	50	09_full_50
09_full_51	09_full	51	09_full_51
09_full_52	09_full	52	09_full_52
09_full_53	09_full	53	09_full_53
09_full_54	09_full	54	09_full_54
09_full_55	09_full	55	09_full_55
09_full_56	09_full	56	09_full_56
09_full_57	09_full	57	09_full_57
09_full_58	09_full	58	09_full_58
09_full_59	09_full	59	09_full_59
09_full_60	09_full	60	09_full_60
09_full_61	09_full	61	09_full_61
09_full_62	09_full	62	09_full_62
09_full_63	09_full	63	09_full_63
09_full_64	09_full	64	09_full_64
09_full_65	09_full	65	09_full_65
09_full_66	09_full	66	09_full_66
09_full_67	09_full	67	09_full_67
09_full_68	09_full	68	09_full_68
09_full_69	09_full	69	09_full_69
09_full_70	09_full	70	09_full_70
09_full_71	09_full	71	09_full_71
09_full_72	09_full	72	09_full_72
09_full_73	09_full	73	09_full_73
09_full_74	09_full	74	09_full_74
09_full_75	09_full	75	09_full_75
09_full_76	09_full	76	09_full_76
09_full_77	09_full	77	09_full_77
09_full_78	09_full	78	09_full_78
09_full_79	09_full	79	09_full_79
09_full_80	09_full	80	09_full_80
09_full_81	09_full	81	09_full_81
09_full_82	09_full	82	09_full_82
09_full_83	09_full	83	09_full_83
09_full_84	09_full	84	09_full_84
09_full_85	09_full	85	09_full_85
09_full_86	09_full	86	09_full_86
09_full_87	09_full	87	09_full_87
09_full_88	09_full	88	09_full_88
09_full_89	09_full	89	09_full_89
09_full_90	09_full	90	09_full_90
09_full_91	09_full	91	09_full_91
09_full_92	09_full	92	09_full_92
09_full_93	09_full	93	09_full_93
09_full_94	09_full	94	09_full_94
09_full_95	09_full	95	09_full_95
10_full_32	10_full	32	10_full_32
10_full_33	10_full	33	10_full_33
10_full_34	10_full	34	10_full_34
10_full_35	10_full	35	10_full_35
10_full_36	10_full	36	10_full_36
10_full_37	10_full	37	10_full_37
10_full_38	10_full	38	10_full_38
10_full_39	10_full	39	10_full_39
10_full_40	10_full	40	10_full_40
10_full_41	10_full	41	10_full_41
10_full_42	10_full	42	10_full_42
10_full_43	10_full	43	10_full_43
10_full_44	10_full	44	10_full_44
10_full_45	10_full	45	10_full_45
10_full_46	10_full	46	10_full_46
10_full_47	10_full	47	10_full_47
10_full_48	10_full	48	10_full_48
10_full_49	10_full	49	10_full_49
10_full_50	10_full	50	10_full_50
10_full_51	10_full	51	10_full_51
10_full_52	10_full	52	10_full_52
10_full_53	10_full	53	10_full_53
10_full_54	10_full	54	10_full_54
10_full_55	10_full	55	10_full_55
10_full_56	10_full	56	10_full_56
10_full_57	10_full	57	10_full_57
10_full_58	10_full	58	10_full_58
10_full_59	10_full	59	10_full_59
10_full_60	10_full	60	10_full_60
10_full_61	10_full	61	10_full_61
10_full_62	10_full	62	10_full_62
10_full_63	10_full	63	10_full_63
10_full_64	10_full	64	10_full_64
10_full_65	10_full	65	10_full_65
10_full_66	10_full	66	10_full_66
10_full_67	10_full	67	10_full_67
10_full_68	10_full	68	10_full_68
10_full_69	10_full	69	10_full_69
10_full_70	10_full	70	10_full_70
10_full_71	10_full	71	10_full_71
10_full_72	10_full	72	10_full_72
10_full_73	10_full	73	10_full_73
10_full_74	10_full	74	10_full_74
10_full_75	10_full	75	10_full_75
10_full_76	10_full	76	10_full_76
10_full_77	10_full	77	10_full_77
10_full_78	10_full	78	10_full_78
10_full_79	10_full	79	10_full_79
10_full_80	10_full	80	10_full_80
10_full_81	10_full	81	10_full_81
10_full_82	10_full	82	10_full_82
10_full_83	10_full	83	10_full_83
10_full_84	10_full	84	10_full_84
10_full_85	10_full	85	10_full_85
10_full_86	10_full	86	10_full_86
10_full_87	10_full	87	10_full_87
10_full_88	10_full	88	10_full_88
10_full_89	10_full	89	10_full_89
10_full_90	10_full	90	10_full_90
10_full_91	10_full	91	10_full_91
10_full_92	10_full	92	10_full_92
10_full_93	10_full	93	10_full_93
10_full_94	10_full	94	10_full_94
10_full_95	10_full	95	10_full_95
11_full_32	11_full	32	11_full_32
11_full_33	11_full	33	11_full_33
11_full_34	11_full	34	11_full_34
11_full_35	11_full	35	11_full_35
11_full_36	11_full	36	11_full_36
11_full_37	11_full	37	11_full_37
11_full_38	11_full	38	11_full_38
11_full_39	11_full	39	11_full_39
11_full_40	11_full	40	11_full_40
11_full_41	11_full	41	11_full_41
11_full_42	11_full	42	11_full_42
11_full_43	11_full	43	11_full_43
11_full_44	11_full	44	11_full_44
11_full_45	11_full	45	11_full_45
11_full_46	11_full	46	11_full_46
11_full_47	11_full	47	11_full_47
11_full_48	11_full	48	11_full_48
11_full_49	11_full	49	11_full_49
11_full_50	11_full	50	11_full_50
11_full_51	11_full	51	11_full_51
11_full_52	11_full	52	11_full_52
11_full_53	11_full	53	11_full_53
11_full_54	11_full	54	11_full_54
11_full_55	11_full	55	11_full_55
11_full_56	11_full	56	11_full_56
11_full_57	11_full	57	11_full_57
11_full_58	11_full	58	11_full_58
11_full_59	11_full	59	11_full_59
11_full_60	11_full	60	11_full_60
11_full_61	11_full	61	11_full_61
11_full_62	11_full	62	11_full_62
11_full_63	11_full	63	11_full_63
11_full_64	11_full	64	11_full_64
11_full_65	11_full	65	11_full_65
11_full_66	11_full	66	11_full_66
11_full_67	11_full	67	11_full_67
11_full_68	11_full	68	11_full_68
11_full_69	11_full	69	11_full_69
11_full_70	11_full	70	11_full_70
11_full_71	11_full	71	11_full_71
11_full_72	11_full	72	11_full_72
11_full_73	11_full	73	11_full_73
11_full_74	11_full	74	11_full_74
11_full_75	11_full	75	11_full_75
11_full_76	11_full	76	11_full_76
11_full_77	11_full	77	11_full_77
11_full_78	11_full	78	11_full_78
11_full_79	11_full	79	11_full_79
11_full_80	11_full	80	11_full_80
11_full_81	11_full	81	11_full_81
11_full_82	11_full	82	11_full_82
11_full_83	11_full	83	11_full_83
11_full_84	11_full	84	11_full_84
11_full_85	11_full	85	11_full_85
11_full_86	11_full	86	11_full_86
11_full_87	11_full	87	11_full_87
11_full_88	11_full	88	11_full_88
11_full_89	11_full	89	11_full_89
11_full_90	11_full	90	11_full_90
11_full_91	11_full	91	11_full_91
11_full_92	11_full	92	11_full_92
11_full_93	11_full	93	11_full_93
11_full_94	11_full	94	11_full_94
11_full_95	11_full	95	11_full_95
12_full_32	12_full	32	12_full_32
12_full_33	12_full	33	12_full_33
12_full_34	12_full	34	12_full_34
12_full_35	12_full	35	12_full_35
12_full_36	12_full	36	12_full_36
12_full_37	12_full	37	12_full_37
12_full_38	12_full	38	12_full_38
12_full_39	12_full	39	12_full_39
12_full_40	12_full	40	12_full_40
12_full_41	12_full	41	12_full_41
12_full_42	12_full	42	12_full_42
12_full_43	12_full	43	12_full_43
12_full_44	12_full	44	12_full_44
12_full_45	12_full	45	12_full_45
12_full_46	12_full	46	12_full_46
12_full_47	12_full	47	12_full_47
12_full_48	12_full	48	12_full_48
12_full_49	12_full	49	12_full_49
12_full_50	12_full	50	12_full_50
12_full_51	12_full	51	12_full_51
12_full_52	12_full	52	12_full_52
12_full_53	12_full	53	12_full_53
12_full_54	12_full	54	12_full_54
12_full_55	12_full	55	12_full_55
12_full_56	12_full	56	12_full_56
12_full_57	12_full	57	12_full_57
12_full_58	12_full	58	12_full_58
12_full_59	12_full	59	12_full_59
12_full_60	12_full	60	12_full_60
12_full_61	12_full	61	12_full_61
12_full_62	12_full	62	12_full_62
12_full_63	12_full	63	12_full_63
12_full_64	12_full	64	12_full_64
12_full_65	12_full	65	12_full_65
12_full_66	12_full	66	12_full_66
12_full_67	12_full	67	12_full_67
12_full_68	12_full	68	12_full_68
12_full_69	12_full	69	12_full_69
12_full_70	12_full	70	12_full_70
12_full_71	12_full	71	12_full_71
12_full_72	12_full	72	12_full_72
12_full_73	12_full	73	12_full_73
12_full_74	12_full	74	12_full_74
12_full_75	12_full	75	12_full_75
12_full_76	12_full	76	12_full_76
12_full_77	12_full	77	12_full_77
12_full_78	12_full	78	12_full_78
12_full_79	12_full	79	12_full_79
12_full_80	12_full	80	12_full_80
12_full_81	12_full	81	12_full_81
12_full_82	12_full	82	12_full_82
12_full_83	12_full	83	12_full_83
12_full_84	12_full	84	12_full_84
12_full_85	12_full	85	12_full_85
12_full_86	12_full	86	12_full_86
12_full_87	12_full	87	12_full_87
12_full_88	12_full	88	12_full_88
12_full_89	12_full	89	12_full_89
12_full_90	12_full	90	12_full_90
12_full_91	12_full	91	12_full_91
12_full_92	12_full	92	12_full_92
12_full_93	12_full	93	12_full_93
12_full_94	12_full	94	12_full_94
12_full_95	12_full	95	12_full_95
12_full_bold_32	12_full_bold	32	12_full_bold_32
12_full_bold_33	12_full_bold	33	12_full_bold_33
12_full_bold_34	12_full_bold	34	12_full_bold_34
12_full_bold_35	12_full_bold	35	12_full_bold_35
12_full_bold_36	12_full_bold	36	12_full_bold_36
12_full_bold_37	12_full_bold	37	12_full_bold_37
12_full_bold_38	12_full_bold	38	12_full_bold_38
12_full_bold_39	12_full_bold	39	12_full_bold_39
12_full_bold_40	12_full_bold	40	12_full_bold_40
12_full_bold_41	12_full_bold	41	12_full_bold_41
12_full_bold_42	12_full_bold	42	12_full_bold_42
12_full_bold_43	12_full_bold	43	12_full_bold_43
12_full_bold_44	12_full_bold	44	12_full_bold_44
12_full_bold_45	12_full_bold	45	12_full_bold_45
12_full_bold_46	12_full_bold	46	12_full_bold_46
12_full_bold_47	12_full_bold	47	12_full_bold_47
12_full_bold_48	12_full_bold	48	12_full_bold_48
12_full_bold_49	12_full_bold	49	12_full_bold_49
12_full_bold_50	12_full_bold	50	12_full_bold_50
12_full_bold_51	12_full_bold	51	12_full_bold_51
12_full_bold_52	12_full_bold	52	12_full_bold_52
12_full_bold_53	12_full_bold	53	12_full_bold_53
12_full_bold_54	12_full_bold	54	12_full_bold_54
12_full_bold_55	12_full_bold	55	12_full_bold_55
12_full_bold_56	12_full_bold	56	12_full_bold_56
12_full_bold_57	12_full_bold	57	12_full_bold_57
12_full_bold_58	12_full_bold	58	12_full_bold_58
12_full_bold_59	12_full_bold	59	12_full_bold_59
12_full_bold_60	12_full_bold	60	12_full_bold_60
12_full_bold_61	12_full_bold	61	12_full_bold_61
12_full_bold_62	12_full_bold	62	12_full_bold_62
12_full_bold_63	12_full_bold	63	12_full_bold_63
12_full_bold_64	12_full_bold	64	12_full_bold_64
12_full_bold_65	12_full_bold	65	12_full_bold_65
12_full_bold_66	12_full_bold	66	12_full_bold_66
12_full_bold_67	12_full_bold	67	12_full_bold_67
12_full_bold_68	12_full_bold	68	12_full_bold_68
12_full_bold_69	12_full_bold	69	12_full_bold_69
12_full_bold_70	12_full_bold	70	12_full_bold_70
12_full_bold_71	12_full_bold	71	12_full_bold_71
12_full_bold_72	12_full_bold	72	12_full_bold_72
12_full_bold_73	12_full_bold	73	12_full_bold_73
12_full_bold_74	12_full_bold	74	12_full_bold_74
12_full_bold_75	12_full_bold	75	12_full_bold_75
12_full_bold_76	12_full_bold	76	12_full_bold_76
12_full_bold_77	12_full_bold	77	12_full_bold_77
12_full_bold_78	12_full_bold	78	12_full_bold_78
12_full_bold_79	12_full_bold	79	12_full_bold_79
12_full_bold_80	12_full_bold	80	12_full_bold_80
12_full_bold_81	12_full_bold	81	12_full_bold_81
12_full_bold_82	12_full_bold	82	12_full_bold_82
12_full_bold_83	12_full_bold	83	12_full_bold_83
12_full_bold_84	12_full_bold	84	12_full_bold_84
12_full_bold_85	12_full_bold	85	12_full_bold_85
12_full_bold_86	12_full_bold	86	12_full_bold_86
12_full_bold_87	12_full_bold	87	12_full_bold_87
12_full_bold_88	12_full_bold	88	12_full_bold_88
12_full_bold_89	12_full_bold	89	12_full_bold_89
12_full_bold_90	12_full_bold	90	12_full_bold_90
12_full_bold_91	12_full_bold	91	12_full_bold_91
12_full_bold_92	12_full_bold	92	12_full_bold_92
12_full_bold_93	12_full_bold	93	12_full_bold_93
12_full_bold_94	12_full_bold	94	12_full_bold_94
12_full_bold_95	12_full_bold	95	12_full_bold_95
13_full_32	13_full	32	13_full_32
13_full_33	13_full	33	13_full_33
13_full_34	13_full	34	13_full_34
13_full_35	13_full	35	13_full_35
13_full_36	13_full	36	13_full_36
13_full_37	13_full	37	13_full_37
13_full_38	13_full	38	13_full_38
13_full_39	13_full	39	13_full_39
13_full_40	13_full	40	13_full_40
13_full_41	13_full	41	13_full_41
13_full_42	13_full	42	13_full_42
13_full_43	13_full	43	13_full_43
13_full_44	13_full	44	13_full_44
13_full_45	13_full	45	13_full_45
13_full_46	13_full	46	13_full_46
13_full_47	13_full	47	13_full_47
13_full_48	13_full	48	13_full_48
13_full_49	13_full	49	13_full_49
13_full_50	13_full	50	13_full_50
13_full_51	13_full	51	13_full_51
13_full_52	13_full	52	13_full_52
13_full_53	13_full	53	13_full_53
13_full_54	13_full	54	13_full_54
13_full_55	13_full	55	13_full_55
13_full_56	13_full	56	13_full_56
13_full_57	13_full	57	13_full_57
13_full_58	13_full	58	13_full_58
13_full_59	13_full	59	13_full_59
13_full_60	13_full	60	13_full_60
13_full_61	13_full	61	13_full_61
13_full_62	13_full	62	13_full_62
13_full_63	13_full	63	13_full_63
13_full_64	13_full	64	13_full_64
13_full_65	13_full	65	13_full_65
13_full_66	13_full	66	13_full_66
13_full_67	13_full	67	13_full_67
13_full_68	13_full	68	13_full_68
13_full_69	13_full	69	13_full_69
13_full_70	13_full	70	13_full_70
13_full_71	13_full	71	13_full_71
13_full_72	13_full	72	13_full_72
13_full_73	13_full	73	13_full_73
13_full_74	13_full	74	13_full_74
13_full_75	13_full	75	13_full_75
13_full_76	13_full	76	13_full_76
13_full_77	13_full	77	13_full_77
13_full_78	13_full	78	13_full_78
13_full_79	13_full	79	13_full_79
13_full_80	13_full	80	13_full_80
13_full_81	13_full	81	13_full_81
13_full_82	13_full	82	13_full_82
13_full_83	13_full	83	13_full_83
13_full_84	13_full	84	13_full_84
13_full_85	13_full	85	13_full_85
13_full_86	13_full	86	13_full_86
13_full_87	13_full	87	13_full_87
13_full_88	13_full	88	13_full_88
13_full_89	13_full	89	13_full_89
13_full_90	13_full	90	13_full_90
13_full_91	13_full	91	13_full_91
13_full_92	13_full	92	13_full_92
13_full_93	13_full	93	13_full_93
13_full_94	13_full	94	13_full_94
13_full_95	13_full	95	13_full_95
14_full_32	14_full	32	14_full_32
14_full_33	14_full	33	14_full_33
14_full_34	14_full	34	14_full_34
14_full_35	14_full	35	14_full_35
14_full_36	14_full	36	14_full_36
14_full_37	14_full	37	14_full_37
14_full_38	14_full	38	14_full_38
14_full_39	14_full	39	14_full_39
14_full_40	14_full	40	14_full_40
14_full_41	14_full	41	14_full_41
14_full_42	14_full	42	14_full_42
14_full_43	14_full	43	14_full_43
14_full_44	14_full	44	14_full_44
14_full_45	14_full	45	14_full_45
14_full_46	14_full	46	14_full_46
14_full_47	14_full	47	14_full_47
14_full_48	14_full	48	14_full_48
14_full_49	14_full	49	14_full_49
14_full_50	14_full	50	14_full_50
14_full_51	14_full	51	14_full_51
14_full_52	14_full	52	14_full_52
14_full_53	14_full	53	14_full_53
14_full_54	14_full	54	14_full_54
14_full_55	14_full	55	14_full_55
14_full_56	14_full	56	14_full_56
14_full_57	14_full	57	14_full_57
14_full_58	14_full	58	14_full_58
14_full_59	14_full	59	14_full_59
14_full_60	14_full	60	14_full_60
14_full_61	14_full	61	14_full_61
14_full_62	14_full	62	14_full_62
14_full_63	14_full	63	14_full_63
14_full_64	14_full	64	14_full_64
14_full_65	14_full	65	14_full_65
14_full_66	14_full	66	14_full_66
14_full_67	14_full	67	14_full_67
14_full_68	14_full	68	14_full_68
14_full_69	14_full	69	14_full_69
14_full_70	14_full	70	14_full_70
14_full_71	14_full	71	14_full_71
14_full_72	14_full	72	14_full_72
14_full_73	14_full	73	14_full_73
14_full_74	14_full	74	14_full_74
14_full_75	14_full	75	14_full_75
14_full_76	14_full	76	14_full_76
14_full_77	14_full	77	14_full_77
14_full_78	14_full	78	14_full_78
14_full_79	14_full	79	14_full_79
14_full_80	14_full	80	14_full_80
14_full_81	14_full	81	14_full_81
14_full_82	14_full	82	14_full_82
14_full_83	14_full	83	14_full_83
14_full_84	14_full	84	14_full_84
14_full_85	14_full	85	14_full_85
14_full_86	14_full	86	14_full_86
14_full_87	14_full	87	14_full_87
14_full_88	14_full	88	14_full_88
14_full_89	14_full	89	14_full_89
14_full_90	14_full	90	14_full_90
14_full_91	14_full	91	14_full_91
14_full_92	14_full	92	14_full_92
14_full_93	14_full	93	14_full_93
14_full_94	14_full	94	14_full_94
14_full_95	14_full	95	14_full_95
16_full_32	16_full	32	16_full_32
16_full_33	16_full	33	16_full_33
16_full_34	16_full	34	16_full_34
16_full_35	16_full	35	16_full_35
16_full_36	16_full	36	16_full_36
16_full_37	16_full	37	16_full_37
16_full_38	16_full	38	16_full_38
16_full_39	16_full	39	16_full_39
16_full_40	16_full	40	16_full_40
16_full_41	16_full	41	16_full_41
16_full_42	16_full	42	16_full_42
16_full_43	16_full	43	16_full_43
16_full_44	16_full	44	16_full_44
16_full_45	16_full	45	16_full_45
16_full_46	16_full	46	16_full_46
16_full_47	16_full	47	16_full_47
16_full_48	16_full	48	16_full_48
16_full_49	16_full	49	16_full_49
16_full_50	16_full	50	16_full_50
16_full_51	16_full	51	16_full_51
16_full_52	16_full	52	16_full_52
16_full_53	16_full	53	16_full_53
16_full_54	16_full	54	16_full_54
16_full_55	16_full	55	16_full_55
16_full_56	16_full	56	16_full_56
16_full_57	16_full	57	16_full_57
16_full_58	16_full	58	16_full_58
16_full_59	16_full	59	16_full_59
16_full_60	16_full	60	16_full_60
16_full_61	16_full	61	16_full_61
16_full_62	16_full	62	16_full_62
16_full_63	16_full	63	16_full_63
16_full_64	16_full	64	16_full_64
16_full_65	16_full	65	16_full_65
16_full_66	16_full	66	16_full_66
16_full_67	16_full	67	16_full_67
16_full_68	16_full	68	16_full_68
16_full_69	16_full	69	16_full_69
16_full_70	16_full	70	16_full_70
16_full_71	16_full	71	16_full_71
16_full_72	16_full	72	16_full_72
16_full_73	16_full	73	16_full_73
16_full_74	16_full	74	16_full_74
16_full_75	16_full	75	16_full_75
16_full_76	16_full	76	16_full_76
16_full_77	16_full	77	16_full_77
16_full_78	16_full	78	16_full_78
16_full_79	16_full	79	16_full_79
16_full_80	16_full	80	16_full_80
16_full_81	16_full	81	16_full_81
16_full_82	16_full	82	16_full_82
16_full_83	16_full	83	16_full_83
16_full_84	16_full	84	16_full_84
16_full_85	16_full	85	16_full_85
16_full_86	16_full	86	16_full_86
16_full_87	16_full	87	16_full_87
16_full_88	16_full	88	16_full_88
16_full_89	16_full	89	16_full_89
16_full_90	16_full	90	16_full_90
16_full_91	16_full	91	16_full_91
16_full_92	16_full	92	16_full_92
16_full_93	16_full	93	16_full_93
16_full_94	16_full	94	16_full_94
16_full_95	16_full	95	16_full_95
18_full_32	18_full	32	18_full_32
18_full_33	18_full	33	18_full_33
18_full_34	18_full	34	18_full_34
18_full_35	18_full	35	18_full_35
18_full_36	18_full	36	18_full_36
18_full_37	18_full	37	18_full_37
18_full_38	18_full	38	18_full_38
18_full_39	18_full	39	18_full_39
18_full_40	18_full	40	18_full_40
18_full_41	18_full	41	18_full_41
18_full_42	18_full	42	18_full_42
18_full_43	18_full	43	18_full_43
18_full_44	18_full	44	18_full_44
18_full_45	18_full	45	18_full_45
18_full_46	18_full	46	18_full_46
18_full_47	18_full	47	18_full_47
18_full_48	18_full	48	18_full_48
18_full_49	18_full	49	18_full_49
18_full_50	18_full	50	18_full_50
18_full_51	18_full	51	18_full_51
18_full_52	18_full	52	18_full_52
18_full_53	18_full	53	18_full_53
18_full_54	18_full	54	18_full_54
18_full_55	18_full	55	18_full_55
18_full_56	18_full	56	18_full_56
18_full_57	18_full	57	18_full_57
18_full_58	18_full	58	18_full_58
18_full_59	18_full	59	18_full_59
18_full_60	18_full	60	18_full_60
18_full_61	18_full	61	18_full_61
18_full_62	18_full	62	18_full_62
18_full_63	18_full	63	18_full_63
18_full_64	18_full	64	18_full_64
18_full_65	18_full	65	18_full_65
18_full_66	18_full	66	18_full_66
18_full_67	18_full	67	18_full_67
18_full_68	18_full	68	18_full_68
18_full_69	18_full	69	18_full_69
18_full_70	18_full	70	18_full_70
18_full_71	18_full	71	18_full_71
18_full_72	18_full	72	18_full_72
18_full_73	18_full	73	18_full_73
18_full_74	18_full	74	18_full_74
18_full_75	18_full	75	18_full_75
18_full_76	18_full	76	18_full_76
18_full_77	18_full	77	18_full_77
18_full_78	18_full	78	18_full_78
18_full_79	18_full	79	18_full_79
18_full_80	18_full	80	18_full_80
18_full_81	18_full	81	18_full_81
18_full_82	18_full	82	18_full_82
18_full_83	18_full	83	18_full_83
18_full_84	18_full	84	18_full_84
18_full_85	18_full	85	18_full_85
18_full_86	18_full	86	18_full_86
18_full_87	18_full	87	18_full_87
18_full_88	18_full	88	18_full_88
18_full_89	18_full	89	18_full_89
18_full_90	18_full	90	18_full_90
18_full_91	18_full	91	18_full_91
18_full_92	18_full	92	18_full_92
18_full_93	18_full	93	18_full_93
18_full_94	18_full	94	18_full_94
18_full_95	18_full	95	18_full_95
20_full_48	20_full	48	20_full_48
20_full_49	20_full	49	20_full_49
20_full_50	20_full	50	20_full_50
20_full_51	20_full	51	20_full_51
20_full_52	20_full	52	20_full_52
20_full_53	20_full	53	20_full_53
20_full_54	20_full	54	20_full_54
20_full_55	20_full	55	20_full_55
20_full_56	20_full	56	20_full_56
20_full_57	20_full	57	20_full_57
24_full_48	24_full	48	24_full_48
24_full_49	24_full	49	24_full_49
24_full_50	24_full	50	24_full_50
24_full_51	24_full	51	24_full_51
24_full_52	24_full	52	24_full_52
24_full_53	24_full	53	24_full_53
24_full_54	24_full	54	24_full_54
24_full_55	24_full	55	24_full_55
24_full_56	24_full	56	24_full_56
24_full_57	24_full	57	24_full_57
_09_full_12_32	_09_full_12	32	_09_full_12_32
_09_full_12_33	_09_full_12	33	_09_full_12_33
_09_full_12_34	_09_full_12	34	_09_full_12_34
_09_full_12_35	_09_full_12	35	_09_full_12_35
_09_full_12_36	_09_full_12	36	_09_full_12_36
_09_full_12_37	_09_full_12	37	_09_full_12_37
_09_full_12_38	_09_full_12	38	_09_full_12_38
_09_full_12_39	_09_full_12	39	_09_full_12_39
_09_full_12_40	_09_full_12	40	_09_full_12_40
_09_full_12_41	_09_full_12	41	_09_full_12_41
_09_full_12_42	_09_full_12	42	_09_full_12_42
_09_full_12_43	_09_full_12	43	_09_full_12_43
_09_full_12_44	_09_full_12	44	_09_full_12_44
_09_full_12_45	_09_full_12	45	_09_full_12_45
_09_full_12_46	_09_full_12	46	_09_full_12_46
_09_full_12_47	_09_full_12	47	_09_full_12_47
_09_full_12_48	_09_full_12	48	_09_full_12_48
_09_full_12_49	_09_full_12	49	_09_full_12_49
_09_full_12_50	_09_full_12	50	_09_full_12_50
_09_full_12_51	_09_full_12	51	_09_full_12_51
_09_full_12_52	_09_full_12	52	_09_full_12_52
_09_full_12_53	_09_full_12	53	_09_full_12_53
_09_full_12_54	_09_full_12	54	_09_full_12_54
_09_full_12_55	_09_full_12	55	_09_full_12_55
_09_full_12_56	_09_full_12	56	_09_full_12_56
_09_full_12_57	_09_full_12	57	_09_full_12_57
_09_full_12_58	_09_full_12	58	_09_full_12_58
_09_full_12_59	_09_full_12	59	_09_full_12_59
_09_full_12_60	_09_full_12	60	_09_full_12_60
_09_full_12_61	_09_full_12	61	_09_full_12_61
_09_full_12_62	_09_full_12	62	_09_full_12_62
_09_full_12_63	_09_full_12	63	_09_full_12_63
_09_full_12_64	_09_full_12	64	_09_full_12_64
_09_full_12_65	_09_full_12	65	_09_full_12_65
_09_full_12_66	_09_full_12	66	_09_full_12_66
_09_full_12_67	_09_full_12	67	_09_full_12_67
_09_full_12_68	_09_full_12	68	_09_full_12_68
_09_full_12_69	_09_full_12	69	_09_full_12_69
_09_full_12_70	_09_full_12	70	_09_full_12_70
_09_full_12_71	_09_full_12	71	_09_full_12_71
_09_full_12_72	_09_full_12	72	_09_full_12_72
_09_full_12_73	_09_full_12	73	_09_full_12_73
_09_full_12_74	_09_full_12	74	_09_full_12_74
_09_full_12_75	_09_full_12	75	_09_full_12_75
_09_full_12_76	_09_full_12	76	_09_full_12_76
_09_full_12_77	_09_full_12	77	_09_full_12_77
_09_full_12_78	_09_full_12	78	_09_full_12_78
_09_full_12_79	_09_full_12	79	_09_full_12_79
_09_full_12_80	_09_full_12	80	_09_full_12_80
_09_full_12_81	_09_full_12	81	_09_full_12_81
_09_full_12_82	_09_full_12	82	_09_full_12_82
_09_full_12_83	_09_full_12	83	_09_full_12_83
_09_full_12_84	_09_full_12	84	_09_full_12_84
_09_full_12_85	_09_full_12	85	_09_full_12_85
_09_full_12_86	_09_full_12	86	_09_full_12_86
_09_full_12_87	_09_full_12	87	_09_full_12_87
_09_full_12_88	_09_full_12	88	_09_full_12_88
_09_full_12_89	_09_full_12	89	_09_full_12_89
_09_full_12_90	_09_full_12	90	_09_full_12_90
_09_full_12_91	_09_full_12	91	_09_full_12_91
_09_full_12_92	_09_full_12	92	_09_full_12_92
_09_full_12_93	_09_full_12	93	_09_full_12_93
_09_full_12_94	_09_full_12	94	_09_full_12_94
_09_full_12_95	_09_full_12	95	_09_full_12_95
_09_full_12_96	_09_full_12	96	_09_full_12_96
_09_full_12_97	_09_full_12	97	_09_full_12_97
_09_full_12_98	_09_full_12	98	_09_full_12_98
_09_full_12_99	_09_full_12	99	_09_full_12_99
_09_full_12_100	_09_full_12	100	_09_full_12_100
_09_full_12_101	_09_full_12	101	_09_full_12_101
_09_full_12_102	_09_full_12	102	_09_full_12_102
_09_full_12_103	_09_full_12	103	_09_full_12_103
_09_full_12_104	_09_full_12	104	_09_full_12_104
_09_full_12_105	_09_full_12	105	_09_full_12_105
_09_full_12_106	_09_full_12	106	_09_full_12_106
_09_full_12_107	_09_full_12	107	_09_full_12_107
_09_full_12_108	_09_full_12	108	_09_full_12_108
_09_full_12_109	_09_full_12	109	_09_full_12_109
_09_full_12_110	_09_full_12	110	_09_full_12_110
_09_full_12_111	_09_full_12	111	_09_full_12_111
_09_full_12_112	_09_full_12	112	_09_full_12_112
_09_full_12_113	_09_full_12	113	_09_full_12_113
_09_full_12_114	_09_full_12	114	_09_full_12_114
_09_full_12_115	_09_full_12	115	_09_full_12_115
_09_full_12_116	_09_full_12	116	_09_full_12_116
_09_full_12_117	_09_full_12	117	_09_full_12_117
_09_full_12_118	_09_full_12	118	_09_full_12_118
_09_full_12_119	_09_full_12	119	_09_full_12_119
_09_full_12_120	_09_full_12	120	_09_full_12_120
_09_full_12_121	_09_full_12	121	_09_full_12_121
_09_full_12_122	_09_full_12	122	_09_full_12_122
_09_full_12_123	_09_full_12	123	_09_full_12_123
_09_full_12_124	_09_full_12	124	_09_full_12_124
_09_full_12_125	_09_full_12	125	_09_full_12_125
_7_full_32	_7_full	32	_7_full_32
_7_full_33	_7_full	33	_7_full_33
_7_full_34	_7_full	34	_7_full_34
_7_full_35	_7_full	35	_7_full_35
_7_full_36	_7_full	36	_7_full_36
_7_full_37	_7_full	37	_7_full_37
_7_full_38	_7_full	38	_7_full_38
_7_full_39	_7_full	39	_7_full_39
_7_full_40	_7_full	40	_7_full_40
_7_full_41	_7_full	41	_7_full_41
_7_full_42	_7_full	42	_7_full_42
_7_full_43	_7_full	43	_7_full_43
_7_full_44	_7_full	44	_7_full_44
_7_full_45	_7_full	45	_7_full_45
_7_full_46	_7_full	46	_7_full_46
_7_full_47	_7_full	47	_7_full_47
_7_full_48	_7_full	48	_7_full_48
_7_full_49	_7_full	49	_7_full_49
_7_full_50	_7_full	50	_7_full_50
_7_full_51	_7_full	51	_7_full_51
_7_full_52	_7_full	52	_7_full_52
_7_full_53	_7_full	53	_7_full_53
_7_full_54	_7_full	54	_7_full_54
_7_full_55	_7_full	55	_7_full_55
_7_full_56	_7_full	56	_7_full_56
_7_full_57	_7_full	57	_7_full_57
_7_full_58	_7_full	58	_7_full_58
_7_full_59	_7_full	59	_7_full_59
_7_full_60	_7_full	60	_7_full_60
_7_full_61	_7_full	61	_7_full_61
_7_full_62	_7_full	62	_7_full_62
_7_full_63	_7_full	63	_7_full_63
_7_full_64	_7_full	64	_7_full_64
_7_full_65	_7_full	65	_7_full_65
_7_full_66	_7_full	66	_7_full_66
_7_full_67	_7_full	67	_7_full_67
_7_full_68	_7_full	68	_7_full_68
_7_full_69	_7_full	69	_7_full_69
_7_full_70	_7_full	70	_7_full_70
_7_full_71	_7_full	71	_7_full_71
_7_full_72	_7_full	72	_7_full_72
_7_full_73	_7_full	73	_7_full_73
_7_full_74	_7_full	74	_7_full_74
_7_full_75	_7_full	75	_7_full_75
_7_full_76	_7_full	76	_7_full_76
_7_full_77	_7_full	77	_7_full_77
_7_full_78	_7_full	78	_7_full_78
_7_full_79	_7_full	79	_7_full_79
_7_full_80	_7_full	80	_7_full_80
_7_full_81	_7_full	81	_7_full_81
_7_full_82	_7_full	82	_7_full_82
_7_full_83	_7_full	83	_7_full_83
_7_full_84	_7_full	84	_7_full_84
_7_full_85	_7_full	85	_7_full_85
_7_full_86	_7_full	86	_7_full_86
_7_full_87	_7_full	87	_7_full_87
_7_full_88	_7_full	88	_7_full_88
_7_full_89	_7_full	89	_7_full_89
_7_full_90	_7_full	90	_7_full_90
_7_full_91	_7_full	91	_7_full_91
_7_full_92	_7_full	92	_7_full_92
_7_full_93	_7_full	93	_7_full_93
_7_full_94	_7_full	94	_7_full_94
_7_full_95	_7_full	95	_7_full_95
\.

