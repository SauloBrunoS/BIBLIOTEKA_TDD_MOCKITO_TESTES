<template>
  <v-app>
    <v-app-bar app color="blue" dark>
      <div class="title-container">
        <img src="@/assets/biblioteka_icon.jpg" alt="Biblioteka Logo" class="logo" />
        <span class="title">Biblioteka</span>
      </div>
    </v-app-bar>
    <v-main>
      <div id="app" class="mt-0">
        <h1 class="welcome-message">Bem-vindo ao sistema Biblioteka</h1>
        <img src="@/assets/biblioteka_icon.jpg" alt="Biblioteka Logo" width="300" class="mt-4" />
        <Home />
        <Notification></Notification>
      </div>
    </v-main>
  </v-app>
</template>

<script lang="ts" setup>
import Home from './views/Home.vue';
import Notification from '@/components/Notification.vue';
</script>

<style scoped>
.title-container {
  display: flex;
  align-items: center;
  margin-left: 20px;
}

.welcome-message {
  font-size: 2rem;
  margin-top: 40px;
}

.logo {
  max-height: 40px;
}

.title {
  margin-left: 15px;
  font-size: 1.5rem;
  font-family: 'Courier New', Courier, monospace;
}

#app {
  text-align: center;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  color: #2c3e50;
  margin-top: 60px;
}

h1 {
  font-size: 2rem;
  margin-top: 20px;
}
</style>