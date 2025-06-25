import { type NextRequest, NextResponse } from "next/server"

export async function GET() {
  // Simular status dos dispositivos
  const devices = [
    {
      id: "device1",
      name: "Motorola G53 - Device 1",
      status: "online",
      lastSeen: new Date(),
      currentCall: null,
      callsToday: 45,
      successRate: 78,
    },
    {
      id: "device2",
      name: "Motorola G53 - Device 2",
      status: "calling",
      lastSeen: new Date(),
      currentCall: "11999999999",
      callsToday: 32,
      successRate: 82,
    },
  ]

  return NextResponse.json(devices)
}

export async function POST(request: NextRequest) {
  try {
    const { deviceId, status, callResult } = await request.json()

    // Atualizar status do dispositivo no banco de dados
    console.log(`Dispositivo ${deviceId} reportou status: ${status}`)

    if (callResult) {
      console.log("Resultado da chamada:", callResult)
      // Salvar resultado da chamada no banco
    }

    return NextResponse.json({ success: true })
  } catch (error) {
    return NextResponse.json({ success: false, error: "Erro ao atualizar status" }, { status: 500 })
  }
}
