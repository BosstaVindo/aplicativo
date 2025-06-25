"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Phone, PhoneCall, Play, Pause, Trash2, Upload, Download, Settings } from "lucide-react"
import { useToast } from "@/hooks/use-toast"

interface PhoneNumber {
  id: string
  number: string
  name?: string
  status: "pending" | "calling" | "answered" | "failed" | "completed"
  attempts: number
  lastCall?: Date
}

interface CallList {
  id: string
  name: string
  numbers: PhoneNumber[]
  created: Date
  status: "active" | "paused" | "completed"
}

interface Device {
  id: string
  name: string
  status: "online" | "offline" | "calling"
  lastSeen: Date
  currentCall?: string
}

export default function AutoDialerPanel() {
  const [callLists, setCallLists] = useState<CallList[]>([])
  const [devices, setDevices] = useState<Device[]>([])
  const [selectedList, setSelectedList] = useState<string>("")
  const [newListName, setNewListName] = useState("")
  const [numbersText, setNumbersText] = useState("")
  const [isRunning, setIsRunning] = useState(false)
  const { toast } = useToast()

  // Simular dados iniciais
  useEffect(() => {
    const mockDevices: Device[] = [
      {
        id: "device1",
        name: "Motorola G53 - Device 1",
        status: "online",
        lastSeen: new Date(),
      },
      {
        id: "device2",
        name: "Motorola G53 - Device 2",
        status: "offline",
        lastSeen: new Date(Date.now() - 300000),
      },
    ]
    setDevices(mockDevices)

    const mockList: CallList = {
      id: "list1",
      name: "Lista de Teste",
      created: new Date(),
      status: "active",
      numbers: [
        { id: "1", number: "11999999999", name: "João Silva", status: "pending", attempts: 0 },
        { id: "2", number: "11888888888", name: "Maria Santos", status: "calling", attempts: 1 },
        { id: "3", number: "11777777777", name: "Pedro Costa", status: "completed", attempts: 1 },
      ],
    }
    setCallLists([mockList])
  }, [])

  const createCallList = async () => {
    if (!newListName.trim() || !numbersText.trim()) {
      toast({
        title: "Erro",
        description: "Nome da lista e números são obrigatórios",
        variant: "destructive",
      })
      return
    }

    const numbers = numbersText
      .split("\n")
      .map((line) => line.trim())
      .filter((line) => line)
      .map((line, index) => {
        const [number, name] = line.split(",").map((s) => s.trim())
        return {
          id: `${Date.now()}-${index}`,
          number: number.replace(/\D/g, ""),
          name: name || "",
          status: "pending" as const,
          attempts: 0,
        }
      })

    const newList: CallList = {
      id: `list-${Date.now()}`,
      name: newListName,
      numbers,
      created: new Date(),
      status: "active",
    }

    setCallLists((prev) => [...prev, newList])
    setNewListName("")
    setNumbersText("")

    toast({
      title: "Lista criada",
      description: `Lista "${newListName}" criada com ${numbers.length} números`,
    })
  }

  const startCalling = async (listId: string) => {
    const list = callLists.find((l) => l.id === listId)
    if (!list) return

    setIsRunning(true)

    // Enviar comando para dispositivos Android
    const command = {
      action: "START_CALLING",
      listId: listId,
      numbers: list.numbers.filter((n) => n.status === "pending"),
      conferenceSize: 6,
      retryAttempts: 3,
    }

    try {
      const response = await fetch("/api/send-command", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(command),
      })

      if (response.ok) {
        toast({
          title: "Chamadas iniciadas",
          description: "Comando enviado para os dispositivos",
        })
      }
    } catch (error) {
      toast({
        title: "Erro",
        description: "Falha ao enviar comando",
        variant: "destructive",
      })
    }
  }

  const stopCalling = async () => {
    setIsRunning(false)

    try {
      await fetch("/api/send-command", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ action: "STOP_CALLING" }),
      })

      toast({
        title: "Chamadas paradas",
        description: "Comando de parada enviado",
      })
    } catch (error) {
      toast({
        title: "Erro",
        description: "Falha ao parar chamadas",
        variant: "destructive",
      })
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "pending":
        return "bg-gray-500"
      case "calling":
        return "bg-blue-500"
      case "answered":
        return "bg-green-500"
      case "failed":
        return "bg-red-500"
      case "completed":
        return "bg-purple-500"
      default:
        return "bg-gray-500"
    }
  }

  const getDeviceStatusColor = (status: string) => {
    switch (status) {
      case "online":
        return "bg-green-500"
      case "offline":
        return "bg-red-500"
      case "calling":
        return "bg-blue-500"
      default:
        return "bg-gray-500"
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Sistema de Discagem Automática</h1>
          <p className="text-gray-600">Painel de controle para gerenciar chamadas automáticas com conferência</p>
        </div>

        <Tabs defaultValue="dashboard" className="space-y-6">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="dashboard">Dashboard</TabsTrigger>
            <TabsTrigger value="lists">Listas</TabsTrigger>
            <TabsTrigger value="devices">Dispositivos</TabsTrigger>
            <TabsTrigger value="settings">Configurações</TabsTrigger>
          </TabsList>

          <TabsContent value="dashboard" className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Dispositivos Online</CardTitle>
                  <Phone className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{devices.filter((d) => d.status === "online").length}</div>
                  <p className="text-xs text-muted-foreground">de {devices.length} dispositivos</p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Chamadas Ativas</CardTitle>
                  <PhoneCall className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{devices.filter((d) => d.status === "calling").length}</div>
                  <p className="text-xs text-muted-foreground">em andamento</p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Listas Ativas</CardTitle>
                  <Upload className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{callLists.filter((l) => l.status === "active").length}</div>
                  <p className="text-xs text-muted-foreground">prontas para discagem</p>
                </CardContent>
              </Card>
            </div>

            <Card>
              <CardHeader>
                <CardTitle>Controle de Chamadas</CardTitle>
                <CardDescription>Inicie ou pare as chamadas automáticas</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center space-x-4">
                  <Label htmlFor="list-select">Lista:</Label>
                  <select
                    id="list-select"
                    value={selectedList}
                    onChange={(e) => setSelectedList(e.target.value)}
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  >
                    <option value="">Selecione uma lista</option>
                    {callLists.map((list) => (
                      <option key={list.id} value={list.id}>
                        {list.name} ({list.numbers.length} números)
                      </option>
                    ))}
                  </select>
                </div>

                <div className="flex space-x-4">
                  {!isRunning ? (
                    <Button
                      onClick={() => selectedList && startCalling(selectedList)}
                      disabled={!selectedList}
                      className="bg-green-600 hover:bg-green-700"
                    >
                      <Play className="w-4 h-4 mr-2" />
                      Iniciar Chamadas
                    </Button>
                  ) : (
                    <Button onClick={stopCalling} variant="destructive">
                      <Pause className="w-4 h-4 mr-2" />
                      Parar Chamadas
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="lists" className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Criar Nova Lista</CardTitle>
                  <CardDescription>Adicione uma nova lista de números para discagem</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <Label htmlFor="list-name">Nome da Lista</Label>
                    <Input
                      id="list-name"
                      value={newListName}
                      onChange={(e) => setNewListName(e.target.value)}
                      placeholder="Ex: Campanha Janeiro 2024"
                    />
                  </div>

                  <div>
                    <Label htmlFor="numbers">Números</Label>
                    <Textarea
                      id="numbers"
                      value={numbersText}
                      onChange={(e) => setNumbersText(e.target.value)}
                      placeholder="11999999999,João Silva&#10;11888888888,Maria Santos&#10;11777777777,Pedro Costa"
                      rows={8}
                    />
                    <p className="text-sm text-muted-foreground mt-1">
                      Um número por linha. Formato: número,nome (nome é opcional)
                    </p>
                  </div>

                  <Button onClick={createCallList} className="w-full">
                    <Upload className="w-4 h-4 mr-2" />
                    Criar Lista
                  </Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Listas Existentes</CardTitle>
                  <CardDescription>Gerencie suas listas de números</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {callLists.map((list) => (
                      <div key={list.id} className="border rounded-lg p-4">
                        <div className="flex items-center justify-between mb-2">
                          <h3 className="font-semibold">{list.name}</h3>
                          <Badge className={getStatusColor(list.status)}>{list.status}</Badge>
                        </div>
                        <p className="text-sm text-muted-foreground mb-2">
                          {list.numbers.length} números • Criada em {list.created.toLocaleDateString()}
                        </p>
                        <div className="flex space-x-2">
                          <Button size="sm" variant="outline">
                            <Download className="w-4 h-4 mr-1" />
                            Exportar
                          </Button>
                          <Button size="sm" variant="outline">
                            Editar
                          </Button>
                          <Button size="sm" variant="destructive">
                            <Trash2 className="w-4 h-4" />
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </div>

            {selectedList && (
              <Card>
                <CardHeader>
                  <CardTitle>Detalhes da Lista</CardTitle>
                </CardHeader>
                <CardContent>
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Número</TableHead>
                        <TableHead>Nome</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Tentativas</TableHead>
                        <TableHead>Última Chamada</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {callLists
                        .find((l) => l.id === selectedList)
                        ?.numbers.map((number) => (
                          <TableRow key={number.id}>
                            <TableCell className="font-mono">{number.number}</TableCell>
                            <TableCell>{number.name || "-"}</TableCell>
                            <TableCell>
                              <Badge className={getStatusColor(number.status)}>{number.status}</Badge>
                            </TableCell>
                            <TableCell>{number.attempts}</TableCell>
                            <TableCell>{number.lastCall ? number.lastCall.toLocaleString() : "-"}</TableCell>
                          </TableRow>
                        ))}
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>
            )}
          </TabsContent>

          <TabsContent value="devices" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Dispositivos Conectados</CardTitle>
                <CardDescription>Status dos dispositivos Android conectados</CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Dispositivo</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Última Conexão</TableHead>
                      <TableHead>Chamada Atual</TableHead>
                      <TableHead>Ações</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {devices.map((device) => (
                      <TableRow key={device.id}>
                        <TableCell className="font-medium">{device.name}</TableCell>
                        <TableCell>
                          <Badge className={getDeviceStatusColor(device.status)}>{device.status}</Badge>
                        </TableCell>
                        <TableCell>{device.lastSeen.toLocaleString()}</TableCell>
                        <TableCell>{device.currentCall || "-"}</TableCell>
                        <TableCell>
                          <Button size="sm" variant="outline">
                            Reiniciar
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="settings" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Configurações do Sistema</CardTitle>
                <CardDescription>Configure o comportamento do discador automático</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="conference-size">Tamanho da Conferência</Label>
                    <Input id="conference-size" type="number" defaultValue="6" min="2" max="10" />
                  </div>

                  <div>
                    <Label htmlFor="retry-attempts">Tentativas de Rediscagem</Label>
                    <Input id="retry-attempts" type="number" defaultValue="3" min="1" max="10" />
                  </div>

                  <div>
                    <Label htmlFor="call-timeout">Timeout da Chamada (segundos)</Label>
                    <Input id="call-timeout" type="number" defaultValue="30" min="10" max="120" />
                  </div>

                  <div>
                    <Label htmlFor="delay-between-calls">Delay Entre Chamadas (segundos)</Label>
                    <Input id="delay-between-calls" type="number" defaultValue="5" min="1" max="60" />
                  </div>
                </div>

                <Button className="w-full">
                  <Settings className="w-4 h-4 mr-2" />
                  Salvar Configurações
                </Button>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  )
}
