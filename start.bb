
SetGfx()

Function SetGfx()
	yn$=Input$( "Use windowed mode?" )
	If Left$( Lower$( yn$ ),1 )="y"
		HidePointer
		Graphics 640,480,32,2
		SetBuffer BackBuffer()
		Return
	EndIf
	
	Graphics 640,480,32,1
	SetBuffer BackBuffer()

	
End Function