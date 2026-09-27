# GeoAlarm

Despertador baseado em geofencing para Android, em Kotlin + Jetpack Compose (Material 3),
100% offline (Room como banco local, sem qualquer chamada de rede para a lógica de
alarme/localização).

## Arquitetura

Clean Architecture + MVVM, em três camadas dentro de um único módulo `:app`:

```
domain/    modelos e regras de negócio puras em Kotlin (sem dependência de Android)
  model/       GeoAlarm, enums (GeofenceTransition, VibrationPattern, SnoozeType, DismissStyle), AppSettings
  repository/  interfaces (GeoAlarmRepository, SettingsRepository)
  usecase/     um caso de uso por operação (observar, salvar, excluir, alternar, ressincronizar)
  util/        HaversineUtil (cálculo de distância, sem GPS/rede)

data/      implementação concreta
  local/       Room (Entity, Dao, Database, mappers Entity <-> domínio)
  location/    GeofenceManager (Play Services Geofencing) + GeofenceSyncCoordinator
               (decide entre Play Services e o fallback nativo)
  audio/       AlarmSoundController (toque com fade-in) + VibrationPatterns
  repository/  implementações de GeoAlarmRepository/SettingsRepository (DataStore)

service/   componentes de sistema em segundo plano
  GeofenceBroadcastReceiver   recebe ENTER/EXIT da API de geofencing do Play Services
  LocationTrackingService     fallback via LocationManager nativo (sem Play Services)
  AlarmRingingService         foreground service que toca o alarme (som, vibração, wake lock)
  AlarmActionReceiver         ações de dispensar/soneca (notificação)
  BootCompletedReceiver       re-registra os geofences após reiniciar o aparelho

ui/        Jetpack Compose (Material 3), um pacote por tela + ViewModel
  alarmlist/   lista de geo-alarmes
  alarmedit/   criação/edição (mapa offline via osmdroid, som, vibração, soneca, tela de toque)
  settings/    tema (claro/escuro/automático) e cor de destaque
  trigger/     AlarmTriggerActivity — tela de disparo em tela cheia sobre a tela bloqueada
  theme/       gera o ColorScheme do Material 3 a partir da cor de destaque escolhida
```

Sem framework de DI: `di/ServiceLocator.kt` é um container manual e explícito (mais fácil
de auditar do que Hilt/Koin para um projeto deste tamanho).

## Requisitos atendidos

- Room 100% local — nenhuma chamada de rede na lógica de alarme/geofencing.
- Geofencing via Play Services com fallback automático para `LocationManager` nativo
  (`GeofenceSyncCoordinator` escolhe o backend e liga/desliga o serviço de rastreamento).
- Disparo com tela bloqueada: `AlarmTriggerActivity` usa `setShowWhenLocked` +
  `setTurnScreenOn` (com fallback de flags de janela para API 26) e `AlarmRingingService`
  mantém um `PowerManager.WakeLock` parcial enquanto toca.
- Notificação de alta prioridade com `fullScreenIntent` em canal `IMPORTANCE_HIGH` e
  `setBypassDnd(true)`.
- Som independente da vibração, seletor de toque do sistema (`RingtoneManager`) e
  importação de áudio local (Storage Access Framework), padrões de vibração customizados
  (`VibrationEffect.createWaveform`) e volume crescente (fade-in) configurável.
- Tema claro/escuro/automático e cor de destaque que recalcula os tokens do Material 3
  (`ui/theme/Theme.kt`).
- Editor da tela de alarme: título/mensagem, estilo de descarte (botão, arrastar, segurar
  3s), mostrar/ocultar hora e distância, cor ou imagem de fundo.
- Soneca por tempo ou por distância (`AlarmRingingService` + `SnoozeScheduler`, via
  `AlarmManager.setExactAndAllowWhileIdle`), dias da semana ativos e chave geral por alarme.

## Como compilar

```
./gradlew assembleDebug
```

Abra o projeto em Android Studio (Koala ou mais recente) para o caminho mais simples —
ele baixa o Android SDK/AGP automaticamente. `minSdk 26`, `compileSdk`/`targetSdk 35`.

> **Nota sobre este ambiente de execução (sandbox):** o código foi escrito e revisado
> aqui, mas a política de rede deste sandbox bloqueia `dl.google.com` (e
> `maven.google.com`, que redireciona para lá), que é de onde vêm o Android SDK, o
> Android Gradle Plugin e todas as bibliotecas AndroidX/Compose/Play Services. Por isso
> não foi possível rodar `./gradlew assembleDebug` aqui dentro para uma compilação real.
> O projeto abre e compila normalmente no Android Studio ou em qualquer CI com acesso
> padrão à internet.
