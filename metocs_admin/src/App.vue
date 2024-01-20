
<!-- .vue 文件可以编写以下三种标签-->

<!--html-->
<template>
  <div class="hello">
    <h1>Hello World！</h1>
  </div>
</template>


<!--逻辑-->
<script lang="ts">
  export default {
    name: "App"
  }
</script>


<!--样式-->

<style>
   .hello{
     background-color: aquamarine;
     box-shadow: 0 0 10px;
     border-radius: 10px;
     padding: 20px;
   }
</style>
